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

class LeverJobProviderTest {

    private LeverJobProvider provider;

    @BeforeEach
    void setUp() {
        JobLocationParser locationParser = new JobLocationParser();
        ExperienceRequirementParser experienceParser = new ExperienceRequirementParser();
        FresherJobEligibilityService eligibilityService = new FresherJobEligibilityService(locationParser, experienceParser);

        provider = new LeverJobProvider(
                new ObjectMapper(),
                eligibilityService,
                new FresherJobClassifier(),
                new SkillRelevanceExtractor()
        );
    }

    @Test
    @DisplayName("Supports LEVER provider")
    void testSupports() {
        assertTrue(provider.supports("LEVER"));
        assertTrue(provider.supports("lever"));
        assertFalse(provider.supports("GREENHOUSE"));
    }

    @Test
    @DisplayName("Normalize Lever job and preserve application URL")
    void testNormalize() {
        CompanySource source = new CompanySource("Palantir", "https://palantir.com/careers", "LEVER", "palantir", "India");
        source.setId(2L);

        RawJobDto raw = new RawJobDto();
        raw.setExternalJobId("lever-999");
        raw.setSourceJobId("lever-999");
        raw.setSourceProvider("LEVER");
        raw.setTitle("Junior Software Engineer - Core Infrastructure");
        raw.setDescription("Entry level position. No prior experience required. Java, REST API, SQL skills.");
        raw.setLocation("Bengaluru, India");
        raw.setCountry("India");
        raw.setApplicationUrl("https://jobs.lever.co/palantir/lever-999");
        raw.setSourceUrl("https://jobs.lever.co/palantir/lever-999");

        AggregatedJob job = provider.normalize(raw, source);

        assertNotNull(job);
        assertEquals("Palantir", job.getCompanyName());
        assertEquals("https://jobs.lever.co/palantir/lever-999", job.getApplicationUrl());
        assertTrue(job.isFresher());
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, job.getEligibilityStatus());
        assertEquals(0, job.getMinimumExperienceYears());
        assertTrue(job.getSkills().contains("Java"));
    }
}
