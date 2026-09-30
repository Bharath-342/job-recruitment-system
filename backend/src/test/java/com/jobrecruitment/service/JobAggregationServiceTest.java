package com.jobrecruitment.service;

import com.jobrecruitment.dto.response.JobStatisticsResponse;
import com.jobrecruitment.dto.response.SyncSummaryDto;
import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.CompanySource;
import com.jobrecruitment.entity.ExperienceLevel;
import com.jobrecruitment.provider.JobProviderFactory;
import com.jobrecruitment.provider.JobSourceProvider;
import com.jobrecruitment.provider.RawJobDto;
import com.jobrecruitment.repository.AggregatedJobRepository;
import com.jobrecruitment.repository.CompanySourceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobAggregationServiceTest {

    @Mock
    private CompanySourceRepository companySourceRepository;

    @Mock
    private AggregatedJobRepository aggregatedJobRepository;

    @Mock
    private JobProviderFactory providerFactory;

    @Mock
    private JobSourceProvider mockProvider;

    @Mock
    private com.jobrecruitment.classifier.FresherJobEligibilityService eligibilityService;

    @InjectMocks
    private JobAggregationService aggregationService;

    private CompanySource source1;
    private CompanySource source2;

    @BeforeEach
    void setUp() {
        source1 = new CompanySource("Company A", "https://a.com/careers", "GREENHOUSE", "comp-a", "India");
        source1.setId(1L);
        source2 = new CompanySource("Company B", "https://b.com/careers", "LEVER", "comp-b", "India");
        source2.setId(2L);
    }

    @Test
    @DisplayName("Get verified statistics calculates unique companies hiring freshers")
    void testGetStatistics() {
        when(aggregatedJobRepository.countStrictFresherJobs()).thenReturn(15L);
        when(aggregatedJobRepository.countDistinctCompaniesHiringFreshers()).thenReturn(6L);
        when(aggregatedJobRepository.countDistinctLocationsHiringFreshers()).thenReturn(4L);
        when(aggregatedJobRepository.countStrictRemoteFresherJobs()).thenReturn(3L);
        when(aggregatedJobRepository.findLatestVerificationTimestamp()).thenReturn(LocalDateTime.now());

        JobStatisticsResponse stats = aggregationService.getStatistics();

        assertNotNull(stats);
        assertEquals(15, stats.getTotalActiveFresherJobs());
        assertEquals(6, stats.getUniqueCompaniesHiring());
        assertEquals(4, stats.getLocations());
        assertEquals(3, stats.getRemoteJobs());
    }

    @Test
    @DisplayName("Provider failure isolation: error in one source does not crash entire sync")
    void testProviderFailureIsolation() throws Exception {
        when(companySourceRepository.findByEnabledTrue()).thenReturn(List.of(source1, source2));

        // Source 1 succeeds
        when(providerFactory.getProvider("GREENHOUSE")).thenReturn(Optional.of(mockProvider));
        RawJobDto rawJob = new RawJobDto();
        rawJob.setExternalJobId("101");
        rawJob.setTitle("Graduate Software Engineer");
        when(mockProvider.fetchJobs(source1)).thenReturn(List.of(rawJob));

        AggregatedJob aggJob = new AggregatedJob();
        aggJob.setExternalJobId("101");
        aggJob.setSourceProvider("GREENHOUSE");
        aggJob.setCompanyName("Company A");
        aggJob.setTitle("Graduate Software Engineer");
        aggJob.setFresher(true);
        aggJob.setExperienceLevel(ExperienceLevel.FRESHER);
        aggJob.setActive(true);
        when(mockProvider.normalize(any(), eq(source1))).thenReturn(aggJob);
        when(eligibilityService.isEligibleForIndianFreshers(any())).thenReturn(true);
        when(aggregatedJobRepository.findBySourceProviderAndExternalJobId("GREENHOUSE", "101")).thenReturn(java.util.Collections.emptyList());

        // Source 2 fails with exception (e.g. timeout / 429)
        when(providerFactory.getProvider("LEVER")).thenThrow(new RuntimeException("Connection timeout"));

        SyncSummaryDto summary = aggregationService.syncAllSources();

        assertEquals(2, summary.getSourcesProcessed());
        assertEquals(1, summary.getSourcesSuccessful());
        assertEquals(1, summary.getSourcesFailed());
        assertEquals(1, summary.getJobsInserted());
        assertEquals(1, summary.getFresherJobsIdentified());
    }
}
