package com.jobrecruitment.service;

import com.jobrecruitment.dto.response.*;
import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.CompanySource;
import com.jobrecruitment.entity.ExperienceLevel;
import com.jobrecruitment.exception.ResourceNotFoundException;
import com.jobrecruitment.provider.JobProviderFactory;
import com.jobrecruitment.provider.JobSourceProvider;
import com.jobrecruitment.provider.RawJobDto;
import com.jobrecruitment.repository.AggregatedJobRepository;
import com.jobrecruitment.repository.AggregatedJobSpecifications;
import com.jobrecruitment.repository.CompanySourceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class JobAggregationService {

    private static final Logger log = LoggerFactory.getLogger(JobAggregationService.class);

    private final CompanySourceRepository companySourceRepository;
    private final AggregatedJobRepository aggregatedJobRepository;
    private final JobProviderFactory providerFactory;

    public JobAggregationService(CompanySourceRepository companySourceRepository,
                                 AggregatedJobRepository aggregatedJobRepository,
                                 JobProviderFactory providerFactory) {
        this.companySourceRepository = companySourceRepository;
        this.aggregatedJobRepository = aggregatedJobRepository;
        this.providerFactory = providerFactory;
    }

    /**
     * Search fresher jobs with multi-criteria filtering and database-level pagination.
     */
    @Transactional(readOnly = true)
    public Page<FresherJobResponse> searchFresherJobs(
            String keyword,
            String location,
            String role,
            String company,
            Boolean remote,
            ExperienceLevel experienceLevel,
            Pageable pageable) {

        Specification<AggregatedJob> spec = AggregatedJobSpecifications.withFilters(
                keyword, location, role, company, remote, experienceLevel, true, true);

        return aggregatedJobRepository.findAll(spec, pageable)
                .map(FresherJobResponse::fromEntity);
    }

    /**
     * General unified search across all aggregated jobs (including all experience levels).
     */
    @Transactional(readOnly = true)
    public Page<FresherJobResponse> searchAllAggregatedJobs(
            String keyword,
            String location,
            String role,
            String company,
            Boolean remote,
            ExperienceLevel experienceLevel,
            Boolean isFresher,
            Pageable pageable) {

        Specification<AggregatedJob> spec = AggregatedJobSpecifications.withFilters(
                keyword, location, role, company, remote, experienceLevel, isFresher, true);

        return aggregatedJobRepository.findAll(spec, pageable)
                .map(FresherJobResponse::fromEntity);
    }

    /**
     * Get single fresher job details by ID.
     */
    @Transactional(readOnly = true)
    public FresherJobResponse getJobById(Long id) {
        AggregatedJob job = aggregatedJobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Fresher job not found with id: " + id));
        return FresherJobResponse.fromEntity(job);
    }

    /**
     * Return verified statistics from the actual production database.
     */
    @Transactional(readOnly = true)
    public JobStatisticsResponse getStatistics() {
        long totalActive = aggregatedJobRepository.countByIsActiveTrueAndIsFresherTrue();
        long uniqueCompanies = aggregatedJobRepository.countDistinctCompaniesHiringFreshers();
        long locations = aggregatedJobRepository.countDistinctLocationsHiringFreshers();
        long remoteJobs = aggregatedJobRepository.countByIsActiveTrueAndIsFresherTrueAndRemoteTrue();
        LocalDateTime latest = aggregatedJobRepository.findLatestVerificationTimestamp();

        return new JobStatisticsResponse(totalActive, uniqueCompanies, locations, remoteJobs,
                latest != null ? latest : LocalDateTime.now());
    }

    /**
     * Return list of unique companies currently hiring freshers.
     */
    @Transactional(readOnly = true)
    public List<String> getCompaniesHiringFreshers() {
        return aggregatedJobRepository.findDistinctCompaniesHiringFreshers();
    }

    /**
     * Return all configured company sources and their status.
     */
    @Transactional(readOnly = true)
    public List<CompanySourceDto> getAllSources() {
        return companySourceRepository.findAll().stream()
                .map(CompanySourceDto::fromEntity)
                .toList();
    }

    /**
     * Toggle source enabled status.
     */
    @Transactional
    public void toggleSource(Long sourceId, boolean enabled) {
        CompanySource source = companySourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found with id: " + sourceId));
        source.setEnabled(enabled);
        companySourceRepository.save(source);
    }

    /**
     * Scheduled synchronization running at configurable intervals (default: 4 hours).
     */
    @Scheduled(fixedDelayString = "${job.sync.interval:14400000}", initialDelay = 60000)
    public void scheduledSync() {
        log.info("Starting scheduled fresher job synchronization...");
        syncAllSources();
    }

    /**
     * Synchronize all enabled sources. Failures in one source do not stop others.
     */
    public SyncSummaryDto syncAllSources() {
        log.info("Sync started for all enabled sources");
        SyncSummaryDto summary = new SyncSummaryDto();

        List<CompanySource> sources = companySourceRepository.findByEnabledTrue();
        summary.setSourcesProcessed(sources.size());

        for (CompanySource source : sources) {
            try {
                syncSingleSourceInternal(source, summary);
                summary.setSourcesSuccessful(summary.getSourcesSuccessful() + 1);
            } catch (Exception e) {
                log.error("Failed to sync source: {} ({}) - {}", source.getCompanyName(), source.getProviderIdentifier(), e.getMessage());
                summary.setSourcesFailed(summary.getSourcesFailed() + 1);
                updateSourceError(source, e.getMessage());
            }
        }

        log.info("Sync completed: Processed={}, Successful={}, Failed={}, JobsFound={}, Inserted={}, Updated={}, Deactivated={}, Freshers={}",
                summary.getSourcesProcessed(), summary.getSourcesSuccessful(), summary.getSourcesFailed(),
                summary.getTotalJobsDiscovered(), summary.getJobsInserted(), summary.getJobsUpdated(),
                summary.getJobsDeactivated(), summary.getFresherJobsIdentified());

        return summary;
    }

    /**
     * Synchronize a specific source by ID.
     */
    public SyncSummaryDto syncSource(Long sourceId) {
        CompanySource source = companySourceRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Source not found with id: " + sourceId));

        SyncSummaryDto summary = new SyncSummaryDto();
        summary.setSourcesProcessed(1);
        try {
            syncSingleSourceInternal(source, summary);
            summary.setSourcesSuccessful(1);
        } catch (Exception e) {
            log.error("Failed to sync source {}: {}", source.getCompanyName(), e.getMessage());
            summary.setSourcesFailed(1);
            updateSourceError(source, e.getMessage());
        }
        return summary;
    }

    @Transactional
    protected void syncSingleSourceInternal(CompanySource source, SyncSummaryDto summary) throws Exception {
        log.info("Provider sync started: {} ({}: {})", source.getCompanyName(), source.getProvider(), source.getProviderIdentifier());
        source.setLastAttemptedSync(LocalDateTime.now());
        source.setStatus("SYNCING");
        companySourceRepository.save(source);

        Optional<JobSourceProvider> providerOpt = providerFactory.getProvider(source.getProvider());
        if (providerOpt.isEmpty()) {
            throw new IllegalArgumentException("No provider implementation found for: " + source.getProvider());
        }

        JobSourceProvider provider = providerOpt.get();
        List<RawJobDto> rawJobs = provider.fetchJobs(source);
        summary.setTotalJobsDiscovered(summary.getTotalJobsDiscovered() + rawJobs.size());

        Set<String> seenExternalIds = new HashSet<>();
        int fresherCount = 0;

        for (RawJobDto raw : rawJobs) {
            seenExternalIds.add(raw.getExternalJobId());
            AggregatedJob normalized = provider.normalize(raw, source);

            if (normalized.isFresher()) {
                fresherCount++;
            }

            // Deduplication strategy
            // 1. By provider + externalJobId
            Optional<AggregatedJob> existingOpt = aggregatedJobRepository
                    .findBySourceProviderAndExternalJobId(normalized.getSourceProvider(), normalized.getExternalJobId());

            // 2. Fallback deduplication by company + title + location
            if (existingOpt.isEmpty()) {
                existingOpt = aggregatedJobRepository
                        .findByCompanyNameIgnoreCaseAndTitleIgnoreCaseAndLocationIgnoreCase(
                                normalized.getCompanyName(), normalized.getTitle(), normalized.getLocation());
                if (existingOpt.isPresent()) {
                    summary.setDuplicatesDetected(summary.getDuplicatesDetected() + 1);
                }
            }

            if (existingOpt.isPresent()) {
                AggregatedJob existing = existingOpt.get();
                existing.setTitle(normalized.getTitle());
                existing.setDescription(normalized.getDescription());
                existing.setLocation(normalized.getLocation());
                existing.setCountry(normalized.getCountry());
                existing.setDepartment(normalized.getDepartment());
                existing.setEmploymentType(normalized.getEmploymentType());
                existing.setApplicationUrl(normalized.getApplicationUrl());
                existing.setSourceUrl(normalized.getSourceUrl());
                existing.setRemote(normalized.isRemote());
                existing.setExperienceLevel(normalized.getExperienceLevel());
                existing.setFresher(normalized.isFresher());
                existing.setFresherConfidence(normalized.getFresherConfidence());
                existing.setSkills(normalized.getSkills());
                existing.setActive(true);
                existing.setLastVerifiedAt(LocalDateTime.now());
                aggregatedJobRepository.save(existing);
                summary.setJobsUpdated(summary.getJobsUpdated() + 1);
            } else {
                aggregatedJobRepository.save(normalized);
                summary.setJobsInserted(summary.getJobsInserted() + 1);
            }
        }

        // Detect inactive/expired jobs: If a job previously existed for this source and is missing from current feed
        List<AggregatedJob> existingSourceJobs = aggregatedJobRepository.findByCompanyId(source.getId());
        for (AggregatedJob existing : existingSourceJobs) {
            if (!seenExternalIds.contains(existing.getExternalJobId()) && existing.isActive()) {
                existing.setActive(false);
                existing.setLastVerifiedAt(LocalDateTime.now());
                aggregatedJobRepository.save(existing);
                summary.setJobsDeactivated(summary.getJobsDeactivated() + 1);
            }
        }

        summary.setFresherJobsIdentified(summary.getFresherJobsIdentified() + fresherCount);

        // Update source success
        source.setStatus("SUCCESS");
        source.setLastSuccessfulSync(LocalDateTime.now());
        source.setTotalJobsFound(rawJobs.size());
        source.setFresherJobsFound(fresherCount);
        source.setErrorMessage(null);
        companySourceRepository.save(source);

        log.info("Provider completed: {} - Discovered={}, Freshers={}",
                source.getCompanyName(), rawJobs.size(), fresherCount);
    }

    @Transactional
    protected void updateSourceError(CompanySource source, String errorMessage) {
        source.setStatus("ERROR");
        source.setErrorMessage(errorMessage != null && errorMessage.length() > 950 ?
                errorMessage.substring(0, 950) : errorMessage);
        source.setLastAttemptedSync(LocalDateTime.now());
        companySourceRepository.save(source);
    }
}
