package com.jobrecruitment.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrecruitment.classifier.FresherJobClassifier;
import com.jobrecruitment.classifier.SkillRelevanceExtractor;
import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.CompanySource;
import com.jobrecruitment.entity.ExperienceLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GreenhouseJobProviderTest {

    private GreenhouseJobProvider provider;

    @BeforeEach
    void setUp() {
        provider = new GreenhouseJobProvider(
                new ObjectMapper(),
                new FresherJobClassifier(),
                new SkillRelevanceExtractor()
        );
    }

    @Test
    @DisplayName("Supports GREENHOUSE provider")
    void testSupports() {
        assertTrue(provider.supports("GREENHOUSE"));
        assertTrue(provider.supports("greenhouse"));
        assertFalse(provider.supports("LEVER"));
    }

    @Test
    @DisplayName("Normalize RawJobDto and preserve official application URL")
    void testNormalizeJob() {
        CompanySource source = new CompanySource("Canonical", "https://canonical.com/careers", "GREENHOUSE", "canonical", "India");
        source.setId(1L);

        RawJobDto raw = new RawJobDto();
        raw.setExternalJobId("8142329");
        raw.setSourceJobId("8142329");
        raw.setSourceProvider("GREENHOUSE");
        raw.setTitle("Graduate Software Engineer, Open Source and Linux");
        raw.setDescription("Join our team. Experience: recent graduate with knowledge of Java, Spring Boot, and Linux.");
        raw.setLocation("Home based - Worldwide");
        raw.setCountry("India");
        raw.setDepartment("Engineering");
        raw.setApplicationUrl("https://job-boards.greenhouse.io/canonical/jobs/8142329");
        raw.setSourceUrl("https://job-boards.greenhouse.io/canonical/jobs/8142329");
        raw.setRemote(true);

        AggregatedJob normalized = provider.normalize(raw, source);

        assertNotNull(normalized);
        assertEquals("Canonical", normalized.getCompanyName());
        assertEquals("Graduate Software Engineer, Open Source and Linux", normalized.getTitle());
        assertEquals("https://job-boards.greenhouse.io/canonical/jobs/8142329", normalized.getApplicationUrl());
        assertTrue(normalized.isFresher());
        assertEquals(ExperienceLevel.FRESHER, normalized.getExperienceLevel());
        assertTrue(normalized.isActive());
        assertTrue(normalized.getSkills().contains("Java"));
        assertTrue(normalized.getSkills().contains("Spring Boot"));
        assertTrue(normalized.getSkills().contains("Linux"));
    }
}
