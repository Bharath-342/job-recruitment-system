package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.EligibilityStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Mandatory unit tests verifying strict 0-year fresher eligibility as specified in Section 34.
 */
class ExperienceRequirementParserTest {

    private ExperienceRequirementParser parser;
    private FresherEligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        parser = new ExperienceRequirementParser();
        eligibilityService = new FresherEligibilityService(parser);
    }

    @Test
    @DisplayName("1. 'Java Developer - 0 years' -> INCLUDE")
    void testJavaDeveloper0Years() {
        var result = eligibilityService.determineEligibility("Java Developer - 0 years", "We are hiring entry level developers.");
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, result.status());
        assertEquals(0, result.minimumExperienceYears());
    }

    @Test
    @DisplayName("2. 'Java Developer - 0-1 years' -> INCLUDE")
    void testJavaDeveloper0To1Years() {
        var result = eligibilityService.determineEligibility("Java Developer", "Required experience: 0-1 years in Java and Spring Boot.");
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, result.status());
        assertEquals(0, result.minimumExperienceYears());
    }

    @Test
    @DisplayName("3. 'Graduate Software Engineer' with no experience required -> INCLUDE")
    void testGraduateSoftwareEngineerNoExperienceRequired() {
        var result = eligibilityService.determineEligibility("Graduate Software Engineer", "No experience required. Training will be provided.");
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, result.status());
        assertEquals(0, result.minimumExperienceYears());
    }

    @Test
    @DisplayName("4. 'Fresh graduates encouraged' with no prior experience -> INCLUDE")
    void testFreshGraduatesEncouragedNoPriorExperience() {
        var result = eligibilityService.determineEligibility("Software Engineer", "Fresh graduates encouraged to apply. No prior experience needed.");
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, result.status());
        assertEquals(0, result.minimumExperienceYears());
    }

    @Test
    @DisplayName("5. 'Software Engineer - 1+ years' -> EXCLUDE")
    void testSoftwareEngineer1PlusYears() {
        var result = eligibilityService.determineEligibility("Software Engineer - 1+ years", "Candidates must have at least 1+ years experience.");
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, result.status());
        assertEquals(1, result.minimumExperienceYears());
    }

    @Test
    @DisplayName("6. 'Java Developer - 2 years' -> EXCLUDE")
    void testJavaDeveloper2Years() {
        var result = eligibilityService.determineEligibility("Java Developer", "Minimum 2 years experience with Java & Spring.");
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, result.status());
        assertEquals(2, result.minimumExperienceYears());
    }

    @Test
    @DisplayName("7. 'Backend Developer - 3 years' -> EXCLUDE")
    void testBackendDeveloper3Years() {
        var result = eligibilityService.determineEligibility("Backend Developer - 3 years", "Strong experience in SQL and microservices.");
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, result.status());
        assertEquals(3, result.minimumExperienceYears());
    }

    @Test
    @DisplayName("8. 'Senior Java Developer' -> EXCLUDE")
    void testSeniorJavaDeveloper() {
        var result = eligibilityService.determineEligibility("Senior Java Developer", "Lead development of core backend systems.");
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, result.status());
    }

    @Test
    @DisplayName("9. 'Software Engineer - 5+ years' -> EXCLUDE")
    void testSoftwareEngineer5PlusYears() {
        var result = eligibilityService.determineEligibility("Software Engineer", "Requires 5+ years building distributed applications.");
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, result.status());
        assertEquals(5, result.minimumExperienceYears());
    }

    @Test
    @DisplayName("10. 'Software Engineer' with no experience information -> EXCLUDE (UNKNOWN)")
    void testSoftwareEngineerNoExperienceInfo() {
        var result = eligibilityService.determineEligibility("Software Engineer", "Develop scalable cloud microservices with team.");
        assertEquals(EligibilityStatus.UNKNOWN, result.status());
        assertNull(result.minimumExperienceYears());
    }

    @Test
    @DisplayName("11. 'Associate Software Engineer' with 2 years required -> EXCLUDE")
    void testAssociateSoftwareEngineerWith2Years() {
        var result = eligibilityService.determineEligibility("Associate Software Engineer", "Requires 2 years professional development experience.");
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, result.status());
        assertEquals(2, result.minimumExperienceYears());
    }

    @Test
    @DisplayName("12. 'Graduate Engineer' with 1 year mandatory -> EXCLUDE")
    void testGraduateEngineerWith1YearMandatory() {
        var result = eligibilityService.determineEligibility("Graduate Engineer", "Candidate must have minimum 1 year of software engineering experience.");
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, result.status());
        assertEquals(1, result.minimumExperienceYears());
    }

    @Test
    @DisplayName("13. Conflicting requirements: 0-1 years AND 2 years mandatory -> EXCLUDE")
    void testConflictingRequirementsStrictestWins() {
        String desc = "Graduate role. 0-1 years. Later: Candidates must have 2 years professional experience.";
        var result = eligibilityService.determineEligibility("Graduate Software Engineer", desc);
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, result.status());
        assertEquals(2, result.minimumExperienceYears());
    }
}
