package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.ExperienceLevel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FresherJobClassifierTest {

    private FresherJobClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new FresherJobClassifier();
    }

    @Test
    @DisplayName("Graduate Software Engineer -> FRESHER")
    void testGraduateSoftwareEngineer() {
        FresherClassificationResult res = classifier.classify("Graduate Software Engineer", "Looking for talented graduates.");
        assertTrue(res.isFresher());
        assertEquals(ExperienceLevel.FRESHER, res.getExperienceLevel());
        assertTrue(res.getConfidence() >= 15);
    }

    @Test
    @DisplayName("Associate Software Engineer -> FRESHER / ENTRY_LEVEL")
    void testAssociateSoftwareEngineer() {
        FresherClassificationResult res = classifier.classify("Associate Software Engineer", "Work with modern Java and Spring tech stack.");
        assertTrue(res.isFresher());
        assertTrue(res.getExperienceLevel() == ExperienceLevel.ENTRY_LEVEL || res.getExperienceLevel() == ExperienceLevel.FRESHER);
    }

    @Test
    @DisplayName("Java Developer - 0-1 years -> FRESHER")
    void testJavaDeveloperZeroOneYears() {
        FresherClassificationResult res = classifier.classify("Java Developer", "Requires 0-1 years of experience with Java and SQL.");
        assertTrue(res.isFresher());
        assertEquals(ExperienceLevel.FRESHER, res.getExperienceLevel());
    }

    @Test
    @DisplayName("Software Engineer - Recent Graduate -> FRESHER")
    void testSoftwareEngineerRecentGraduate() {
        FresherClassificationResult res = classifier.classify("Software Engineer", "Open to recent graduates with strong problem solving skills.");
        assertTrue(res.isFresher());
        assertEquals(ExperienceLevel.FRESHER, res.getExperienceLevel());
    }

    @Test
    @DisplayName("Software Engineer - 5+ years -> NOT FRESHER")
    void testSoftwareEngineerFivePlusYears() {
        FresherClassificationResult res = classifier.classify("Software Engineer", "Must have 5+ years of experience in distributed systems.");
        assertFalse(res.isFresher());
        assertEquals(ExperienceLevel.EXPERIENCED, res.getExperienceLevel());
    }

    @Test
    @DisplayName("Senior Java Developer -> NOT FRESHER")
    void testSeniorJavaDeveloper() {
        FresherClassificationResult res = classifier.classify("Senior Java Developer", "Lead backend engineering initiatives.");
        assertFalse(res.isFresher());
        assertEquals(ExperienceLevel.SENIOR, res.getExperienceLevel());
    }

    @Test
    @DisplayName("Lead Software Engineer -> NOT FRESHER")
    void testLeadSoftwareEngineer() {
        FresherClassificationResult res = classifier.classify("Lead Software Engineer", "Guide architecture and mentor junior developers.");
        assertFalse(res.isFresher());
        assertEquals(ExperienceLevel.SENIOR, res.getExperienceLevel());
    }

    @Test
    @DisplayName("Principal Engineer -> NOT FRESHER")
    void testPrincipalEngineer() {
        FresherClassificationResult res = classifier.classify("Principal Engineer", "Drive company-wide technical strategy.");
        assertFalse(res.isFresher());
        assertEquals(ExperienceLevel.SENIOR, res.getExperienceLevel());
    }

    @Test
    @DisplayName("Software Engineer with unclear experience -> UNKNOWN")
    void testSoftwareEngineerUnclear() {
        FresherClassificationResult res = classifier.classify("Software Engineer", "Building high scale backend systems.");
        assertFalse(res.isFresher());
        assertEquals(ExperienceLevel.UNKNOWN, res.getExperienceLevel());
    }
}
