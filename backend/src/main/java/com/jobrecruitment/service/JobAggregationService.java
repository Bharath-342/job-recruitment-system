package com.jobrecruitment.service;

import com.jobrecruitment.dto.response.*;
import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.CompanySource;
import com.jobrecruitment.entity.EligibilityStatus;
import com.jobrecruitment.entity.ExperienceLevel;
import com.jobrecruitment.entity.LocationClassification;
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
    private final com.jobrecruitment.classifier.FresherJobEligibilityService eligibilityService;
    private final com.jobrecruitment.repository.JobDiscoveryRecordRepository jobDiscoveryRecordRepository;

    public JobAggregationService(CompanySourceRepository companySourceRepository,
                                 AggregatedJobRepository aggregatedJobRepository,
                                 JobProviderFactory providerFactory,
                                 com.jobrecruitment.classifier.FresherJobEligibilityService eligibilityService,
                                 com.jobrecruitment.repository.JobDiscoveryRecordRepository jobDiscoveryRecordRepository) {
        this.companySourceRepository = companySourceRepository;
        this.aggregatedJobRepository = aggregatedJobRepository;
        this.providerFactory = providerFactory;
        this.eligibilityService = eligibilityService;
        this.jobDiscoveryRecordRepository = jobDiscoveryRecordRepository;
    }

    /**
     * Search fresher jobs with strict India + strict 0-year filtering, Java/IT relevance, and database-level pagination.
     */
    @Transactional(readOnly = true)
    public Page<FresherJobResponse> searchFresherJobs(
            String keyword,
            String location,
            String role,
            String company,
            Boolean remote,
            ExperienceLevel experienceLevel,
            com.jobrecruitment.entity.RoleCategory roleCategory,
            Pageable pageable) {

        Specification<AggregatedJob> spec = AggregatedJobSpecifications.withFilters(
                keyword, location, role, company, remote, experienceLevel, roleCategory, true, true);

        return aggregatedJobRepository.findAll(spec, pageable)
                .map(FresherJobResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public Page<FresherJobResponse> searchFresherJobs(
            String keyword,
            String location,
            String role,
            String company,
            Boolean remote,
            ExperienceLevel experienceLevel,
            Pageable pageable) {
        return searchFresherJobs(keyword, location, role, company, remote, experienceLevel, null, pageable);
    }

    /**
     * General unified search across all aggregated jobs.
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
        long totalActive = aggregatedJobRepository.countStrictFresherJobs();
        long uniqueCompanies = aggregatedJobRepository.countDistinctCompaniesHiringFreshers();
        long locations = aggregatedJobRepository.countDistinctLocationsHiringFreshers();
        long remoteJobs = aggregatedJobRepository.countStrictRemoteFresherJobs();
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
     * Return paginated directory of companies currently hiring freshers.
     */
    @Transactional(readOnly = true)
    public Page<CompanyDirectoryItemDto> getCompaniesDirectory(Pageable pageable) {
        return aggregatedJobRepository.findCompaniesHiringFreshers(pageable);
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
     * Scheduled synchronization running at configurable intervals (default: 30 minutes).
     */
    @Scheduled(fixedDelayString = "${job.sync.interval:1800000}", initialDelay = 15000)
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
            // Small delay between sources to balance rate-limits and throughput
            try { Thread.sleep(300); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); break; }
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

            boolean isIndia = normalized.getLocationClassification() == LocationClassification.INDIA &&
                              "INDIA".equalsIgnoreCase(normalized.getCountry());
            if (isIndia) {
                summary.setJobsFromIndia(summary.getJobsFromIndia() + 1);
            }

            // Central Eligibility Rule (Section 12)
            boolean isEligible = eligibilityService.isEligibleForIndianFreshers(normalized);

            if (isEligible) {
                fresherCount++;
                log.info("APPROVED Indian Fresher Job: [{}] {} in {}", normalized.getCompanyName(), normalized.getTitle(), normalized.getLocation());
            } else {
                String rejectionReason = "UNKNOWN";
                if (normalized.getLocationClassification() == LocationClassification.NON_INDIA) {
                    rejectionReason = "FOREIGN_COUNTRY";
                    log.info("REJECTED_FOREIGN_COUNTRY: [{}] {} in {}", normalized.getCompanyName(), normalized.getTitle(), normalized.getLocation());
                    summary.setJobsRejectedForeign(summary.getJobsRejectedForeign() + 1);
                } else if (normalized.getLocationClassification() == LocationClassification.UNKNOWN) {
                    rejectionReason = "UNKNOWN_COUNTRY";
                    log.info("REJECTED_UNKNOWN_COUNTRY: [{}] {} in {}", normalized.getCompanyName(), normalized.getTitle(), normalized.getLocation());
                    summary.setJobsRejectedLocationUnknown(summary.getJobsRejectedLocationUnknown() + 1);
                } else if (normalized.getEligibilityStatus() == EligibilityStatus.NOT_ELIGIBLE) {
                    rejectionReason = "EXPERIENCE_REQUIRED";
                    log.info("REJECTED_EXPERIENCE_REQUIRED: [{}] {} requiring {} yrs", normalized.getCompanyName(), normalized.getTitle(), normalized.getMinimumExperienceYears());
                    summary.setJobsRejectedExperienceGreaterThanZero(summary.getJobsRejectedExperienceGreaterThanZero() + 1);
                } else if (normalized.getEligibilityStatus() == EligibilityStatus.UNKNOWN) {
                    rejectionReason = "UNKNOWN_EXPERIENCE";
                    log.info("REJECTED_UNKNOWN_EXPERIENCE: [{}] {}", normalized.getCompanyName(), normalized.getTitle());
                    summary.setJobsRejectedExperienceUnknown(summary.getJobsRejectedExperienceUnknown() + 1);
                }

                // Section 20: Keep rejected jobs in JobDiscoveryRecord
                try {
                    jobDiscoveryRecordRepository.save(new com.jobrecruitment.entity.JobDiscoveryRecord(
                            normalized.getSourceProvider(),
                            normalized.getExternalJobId(),
                            normalized.getCompanyName(),
                            normalized.getTitle(),
                            normalized.getLocation(),
                            normalized.getExperienceText(),
                            rejectionReason
                    ));
                } catch (Exception ignored) {}
            }

            // Deduplication strategy
            // 1. By provider + externalJobId (returns List to avoid crash on existing duplicates)
            List<AggregatedJob> existingByProviderAndId = aggregatedJobRepository
                    .findBySourceProviderAndExternalJobId(normalized.getSourceProvider(), normalized.getExternalJobId());
            Optional<AggregatedJob> existingOpt = existingByProviderAndId.isEmpty() ? Optional.empty() : Optional.of(existingByProviderAndId.get(0));

            // 2. Fallback deduplication by company + title + location (returns List to avoid unique-result crash)
            if (existingOpt.isEmpty()) {
                List<AggregatedJob> matches = aggregatedJobRepository
                        .findByCompanyNameIgnoreCaseAndTitleIgnoreCaseAndLocationIgnoreCase(
                                normalized.getCompanyName(), normalized.getTitle(), normalized.getLocation());
                if (!matches.isEmpty()) {
                    existingOpt = Optional.of(matches.get(0));
                    log.info("REJECTED_DUPLICATE: [{}] {} matches existing ID {}", normalized.getCompanyName(), normalized.getTitle(), matches.get(0).getId());
                    summary.setDuplicatesDetected(summary.getDuplicatesDetected() + 1);
                    try {
                        jobDiscoveryRecordRepository.save(new com.jobrecruitment.entity.JobDiscoveryRecord(
                                normalized.getSourceProvider(),
                                normalized.getExternalJobId(),
                                normalized.getCompanyName(),
                                normalized.getTitle(),
                                normalized.getLocation(),
                                normalized.getExperienceText(),
                                "DUPLICATE"
                        ));
                    } catch (Exception ignored) {}
                }
            }

            if (existingOpt.isPresent()) {
                AggregatedJob existing = existingOpt.get();
                existing.setTitle(normalized.getTitle());
                existing.setDescription(normalized.getDescription());
                existing.setLocation(normalized.getLocation());
                existing.setCountry(normalized.getCountry());
                existing.setCountryCode(normalized.getCountryCode());
                existing.setCountryName(normalized.getCountryName());
                existing.setState(normalized.getState());
                existing.setCity(normalized.getCity());
                existing.setLocationClassification(normalized.getLocationClassification());
                existing.setDepartment(normalized.getDepartment());
                existing.setEmploymentType(normalized.getEmploymentType());
                existing.setApplicationUrl(normalized.getApplicationUrl());
                existing.setSourceUrl(normalized.getSourceUrl());
                existing.setRemote(normalized.isRemote());
                existing.setExperienceLevel(normalized.getExperienceLevel());
                existing.setFresher(isEligible);
                existing.setEligibilityStatus(normalized.getEligibilityStatus());
                existing.setMinimumExperienceYears(normalized.getMinimumExperienceYears());
                existing.setMaximumExperienceYears(normalized.getMaximumExperienceYears());
                existing.setExperienceText(normalized.getExperienceText());
                existing.setFresherConfidence(normalized.getFresherConfidence());
                existing.setSkills(normalized.getSkills());
                existing.setRoleCategory(normalized.getRoleCategory());
                existing.setTechnologyMatch(normalized.getTechnologyMatch());
                existing.setRelevanceScore(normalized.getRelevanceScore());
                existing.setLastSeenAt(LocalDateTime.now());
                existing.setLastSyncedAt(LocalDateTime.now());
                existing.setActive(isEligible); // Foreign, non-fresher, or non-IT jobs must NOT remain active
                existing.setLastVerifiedAt(LocalDateTime.now());
                aggregatedJobRepository.save(existing);
                summary.setJobsUpdated(summary.getJobsUpdated() + 1);
            } else if (isEligible) {
                // Section 12: Only jobs returning true may be stored/returned as Fresher Jobs
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
                try {
                    jobDiscoveryRecordRepository.save(new com.jobrecruitment.entity.JobDiscoveryRecord(
                            existing.getSourceProvider(),
                            existing.getExternalJobId(),
                            existing.getCompanyName(),
                            existing.getTitle(),
                            existing.getLocation(),
                            existing.getExperienceText(),
                            "EXPIRED"
                    ));
                } catch (Exception ignored) {}
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
