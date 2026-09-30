package com.jobrecruitment.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrecruitment.classifier.*;
import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.CompanySource;
import com.jobrecruitment.entity.EligibilityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AshbyJobProviderTest {

    private AshbyJobProvider provider;

    @BeforeEach
    void setUp() {
        JobLocationParser locationParser = new JobLocationParser();
        ExperienceRequirementParser experienceParser = new ExperienceRequirementParser();
        FresherJobEligibilityService eligibilityService = new FresherJobEligibilityService(locationParser, experienceParser);

        provider = new AshbyJobProvider(
                new ObjectMapper(),
                eligibilityService,
                new FresherJobClassifier(),
                new SkillRelevanceExtractor()
        );
    }

    @Test
    @DisplayName("Supports ASHBY provider")
    void testSupports() {
        assertTrue(provider.supports("ASHBY"));
        assertTrue(provider.supports("ashby"));
        assertFalse(provider.supports("GREENHOUSE"));
    }

    @Test
    @DisplayName("Normalize Ashby job and preserve application URL")
    void testNormalize() {
        CompanySource source = new CompanySource("Sentry", "https://sentry.io/careers", "ASHBY", "sentry", "India");
        source.setId(3L);

        RawJobDto raw = new RawJobDto();
        raw.setExternalJobId("ashby-777");
        raw.setSourceJobId("ashby-777");
        raw.setSourceProvider("ASHBY");
        raw.setTitle("Associate Software Engineer, Platform");
        raw.setDescription("Fresh graduates welcome with 0 years experience. Hands-on experience with Python, React, and Linux.");
        raw.setLocation("Remote - India");
        raw.setCountry("India");
        raw.setRemote(true);
        raw.setApplicationUrl("https://jobs.ashbyhq.com/sentry/ashby-777");
        raw.setSourceUrl("https://jobs.ashbyhq.com/sentry/ashby-777");

        AggregatedJob job = provider.normalize(raw, source);

        assertNotNull(job);
        assertEquals("Sentry", job.getCompanyName());
        assertEquals("https://jobs.ashbyhq.com/sentry/ashby-777", job.getApplicationUrl());
        assertTrue(job.isFresher());
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, job.getEligibilityStatus());
        assertEquals(0, job.getMinimumExperienceYears());
        assertTrue(job.isRemote());
        assertTrue(job.getSkills().contains("React"));
    }
}
