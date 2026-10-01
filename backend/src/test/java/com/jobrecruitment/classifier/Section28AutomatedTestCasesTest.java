package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.EligibilityStatus;
import com.jobrecruitment.entity.LocationClassification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * AUTOMATED TEST CASES strictly required by Section 28 of Master Rectification:
 *
 * 1. Hyderabad, India + 0 years -> PASS
 * 2. Bengaluru, India + no experience -> PASS
 * 3. India Remote + 0 years -> PASS
 * 4. New York, USA + 0 years -> FAIL
 * 5. London, UK + 0 years -> FAIL
 * 6. Toronto, Canada + 0 years -> FAIL
 * 7. Singapore + 0 years -> FAIL
 * 8. Remote - USA + 0 years -> FAIL
 * 9. Remote - Global + 0 years -> FAIL
 * 10. Unknown location + 0 years -> FAIL
 * 11. Hyderabad, India + 2 years -> FAIL
 * 12. Bengaluru, India + 3 years -> FAIL
 * 13. Hyderabad, India + unknown experience -> FAIL
 */
class Section28AutomatedTestCasesTest {

    private JobLocationParser locationParser;
    private ExperienceRequirementParser experienceParser;
    private CountryNormalizer countryNormalizer;
    private IndiaJobLocationValidator locationValidator;
    private FresherJobEligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        locationParser = new JobLocationParser();
        experienceParser = new ExperienceRequirementParser();
        countryNormalizer = new CountryNormalizer();
        locationValidator = new IndiaJobLocationValidator(locationParser, countryNormalizer);
        eligibilityService = new FresherJobEligibilityService(locationParser, experienceParser, locationValidator, countryNormalizer);
    }

    private boolean evaluateJob(String location, String expText) {
        return evaluateJobWithTitle("Software Engineer", location, expText);
    }

    private boolean evaluateJobWithTitle(String title, String location, String expText) {
        var decision = eligibilityService.evaluateEligibility(title, expText, location);
        if (!decision.isEligible()) {
            return false;
        }

        // Also test the central entity checker (Section 12)
        AggregatedJob job = new AggregatedJob();
        job.setTitle(title);
        job.setLocation(location);
        job.setCountry(decision.location().country());
        job.setLocationClassification(decision.location().classification());
        job.setEligibilityStatus(decision.experience().experienceClassification());
        job.setMinimumExperienceYears(decision.experience().minimumExperienceYears());
        job.setActive(true);

        return eligibilityService.isEligibleForIndianFreshers(job);
    }

    @Test
    @DisplayName("1. Hyderabad, India + 0 years -> PASS")
    void test1_HyderabadIndiaZeroYears() {
        boolean passed = evaluateJob("Hyderabad, India", "0 years experience required. Freshers welcome.");
        assertTrue(passed, "Hyderabad, India + 0 years must PASS");
    }

    @Test
    @DisplayName("2. Bengaluru, India + no experience -> PASS")
    void test2_BengaluruIndiaNoExperience() {
        boolean passed = evaluateJob("Bengaluru, India", "Entry level opening. No experience required.");
        assertTrue(passed, "Bengaluru, India + no experience must PASS");
    }

    @Test
    @DisplayName("3. India Remote + 0 years -> PASS")
    void test3_IndiaRemoteZeroYears() {
        boolean passed = evaluateJob("India Remote", "0 years experience required. Fresh graduates apply.");
        assertTrue(passed, "India Remote + 0 years must PASS");
    }

    @Test
    @DisplayName("4. New York, USA + 0 years -> FAIL")
    void test4_NewYorkUSAZeroYears() {
        boolean passed = evaluateJob("New York, USA", "0 years experience required.");
        assertFalse(passed, "New York, USA + 0 years must FAIL");
    }

    @Test
    @DisplayName("5. London, UK + 0 years -> FAIL")
    void test5_LondonUKZeroYears() {
        boolean passed = evaluateJob("London, UK", "0 years experience required. Graduate role.");
        assertFalse(passed, "London, UK + 0 years must FAIL");
    }

    @Test
    @DisplayName("6. Toronto, Canada + 0 years -> FAIL")
    void test6_TorontoCanadaZeroYears() {
        boolean passed = evaluateJob("Toronto, Canada", "0 years experience required.");
        assertFalse(passed, "Toronto, Canada + 0 years must FAIL");
    }

    @Test
    @DisplayName("7. Singapore + 0 years -> FAIL")
    void test7_SingaporeZeroYears() {
        boolean passed = evaluateJob("Singapore", "0 years experience required.");
        assertFalse(passed, "Singapore + 0 years must FAIL");
    }

    @Test
    @DisplayName("8. Remote - USA + 0 years -> FAIL")
    void test8_RemoteUSAZeroYears() {
        boolean passed = evaluateJob("Remote - USA", "0 years experience required.");
        assertFalse(passed, "Remote - USA + 0 years must FAIL");
    }

    @Test
    @DisplayName("9. Remote - Global + 0 years -> FAIL")
    void test9_RemoteGlobalZeroYears() {
        boolean passed = evaluateJob("Remote - Global", "0 years experience required.");
        assertFalse(passed, "Remote - Global + 0 years must FAIL");
    }

    @Test
    @DisplayName("10. Unknown location + 0 years -> FAIL")
    void test10_UnknownLocationZeroYears() {
        boolean passed = evaluateJob("Remote", "0 years experience required.");
        assertFalse(passed, "Unknown location + 0 years must FAIL");

        boolean passedBlank = evaluateJob("", "0 years experience required.");
        assertFalse(passedBlank, "Blank location + 0 years must FAIL");
    }

    @Test
    @DisplayName("11. Hyderabad, India + 2 years -> FAIL")
    void test11_HyderabadIndiaTwoYears() {
        boolean passed = evaluateJob("Hyderabad, India", "Must have 2 years of experience with Java.");
        assertFalse(passed, "Hyderabad, India + 2 years must FAIL");
    }

    @Test
    @DisplayName("12. Bengaluru, India + 3 years -> FAIL")
    void test12_BengaluruIndiaThreeYears() {
        boolean passed = evaluateJob("Bengaluru, India", "Minimum 3 years of hands-on software development experience.");
        assertFalse(passed, "Bengaluru, India + 3 years must FAIL");
    }

    @Test
    @DisplayName("13. Hyderabad, India + unknown experience -> FAIL")
    void test13_HyderabadIndiaUnknownExperience() {
        boolean passed = evaluateJob("Hyderabad, India", "Looking for talented passionate developers to join our team.");
        assertFalse(passed, "Hyderabad, India + unknown experience must FAIL");
    }

    @Test
    @DisplayName("14. Pune, India + 0-1 years -> PASS (Section 35)")
    void test14_PuneIndiaZeroToOneYears() {
        boolean passed = evaluateJob("Pune, India", "Looking for candidates with 0-1 years of experience in Java / Python.");
        assertTrue(passed, "Pune, India + 0-1 years must PASS");
    }

    @Test
    @DisplayName("15. Section 9 Conflict Rule: Graduate Software Engineer + Minimum 2 years experience -> FAIL")
    void test15_GraduateTitleWithTwoYearsExperienceConflict() {
        boolean passed = evaluateJobWithTitle("Graduate Software Engineer", "Hyderabad, India", "Minimum 2 years experience required.");
        assertFalse(passed, "Detailed experience requirement must override graduate title per Section 9");
    }

    @Test
    @DisplayName("16. Bangalore + Software Engineer - Winter Intern (2026 Graduates) -> PASS (Section 8 & 36)")
    void test16_BangaloreWinterIntern() {
        boolean passed = evaluateJobWithTitle("Software Engineer - Winter Intern", "Bangalore, India", "Bachelor's degree in Computer Science, 2026 graduates. General-purpose languages Java/Python.");
        assertTrue(passed, "Bangalore Winter Intern for 2026 graduates must PASS");
    }

    // ==========================================
    // SECTION 33: CRITICAL EXAMPLES TEST SUITE
    // ==========================================
    @Test
    @DisplayName("Section 33 VALID: Java Developer, Hyderabad, 0 years -> PASS")
    void testSection33_JavaDeveloperHyderabadZeroYears() {
        boolean passed = evaluateJobWithTitle("Java Developer", "Hyderabad, India", "0 years experience. Freshers welcome with Java, OOP, and SQL knowledge.");
        assertTrue(passed, "Java Developer, Hyderabad, 0 years must PASS");
    }

    @Test
    @DisplayName("Section 33 VALID: Associate Software Engineer, Bengaluru, Freshers, Java/Spring Boot -> PASS")
    void testSection33_AssociateSoftwareEngineerBengaluruFreshers() {
        boolean passed = evaluateJobWithTitle("Associate Software Engineer", "Bengaluru, India", "Freshers eligible. Skills: Java, Spring Boot, React, and REST APIs.");
        assertTrue(passed, "Associate Software Engineer, Bengaluru, Freshers, Java/Spring Boot must PASS");
    }

    @Test
    @DisplayName("Section 33 VALID: Graduate Software Engineer, Pune, No experience required, Java -> PASS")
    void testSection33_GraduateSoftwareEngineerPuneNoExp() {
        boolean passed = evaluateJobWithTitle("Graduate Software Engineer", "Pune, India", "No experience required. Looking for fresh graduates with Core Java.");
        assertTrue(passed, "Graduate Software Engineer, Pune, No experience required, Java must PASS");
    }

    @Test
    @DisplayName("Section 33 INVALID: Java Developer, London, 0 years -> FAIL")
    void testSection33_JavaDeveloperLondonZeroYears() {
        boolean passed = evaluateJobWithTitle("Java Developer", "London, UK", "0 years experience required. Freshers welcome.");
        assertFalse(passed, "Java Developer, London, 0 years must FAIL (foreign location)");
    }

    @Test
    @DisplayName("Section 33 INVALID: Software Engineer, New York, Fresh graduate -> FAIL")
    void testSection33_SoftwareEngineerNewYorkFreshGrad() {
        boolean passed = evaluateJobWithTitle("Software Engineer", "New York, USA", "Fresh graduate role. 0-1 years experience.");
        assertFalse(passed, "Software Engineer, New York, Fresh graduate must FAIL (foreign location)");
    }

    @Test
    @DisplayName("Section 33 INVALID: Java Developer, Hyderabad, 2 years -> FAIL")
    void testSection33_JavaDeveloperHyderabadTwoYears() {
        boolean passed = evaluateJobWithTitle("Java Developer", "Hyderabad, India", "Minimum 2 years of Java development experience required.");
        assertFalse(passed, "Java Developer, Hyderabad, 2 years must FAIL (experience > 0)");
    }

    @Test
    @DisplayName("Section 33 INVALID: HR Executive, Hyderabad, 0 years -> FAIL")
    void testSection33_HRExecutiveHyderabadZeroYears() {
        boolean passed = evaluateJobWithTitle("HR Executive", "Hyderabad, India", "0 years experience. Fresh MBA graduates in HR.");
        assertFalse(passed, "HR Executive, Hyderabad, 0 years must FAIL (non-IT role)");
    }

    // ==========================================
    // BATCH ELIGIBILITY TESTS (2026 AND BELOW ALLOWED, >2026 EXCLUDED)
    // ==========================================
    @Test
    @DisplayName("Batch 2026 VALID: 2026 graduates, Pune, Java -> PASS")
    void testBatch2026_GraduatesPune() {
        boolean passed = evaluateJobWithTitle("Software Engineer", "Pune, India", "2026 graduates eligible. Knowledge of Java and Spring Boot.");
        assertTrue(passed, "2026 graduates in Pune must PASS");
    }

    @Test
    @DisplayName("Batch 2026 VALID: Batch of 2026, Bengaluru -> PASS")
    void testBatch2026_BatchOf2026Bengaluru() {
        boolean passed = evaluateJobWithTitle("Associate Software Developer", "Bengaluru, India", "Batch of 2026 hiring drive for Core Java developers.");
        assertTrue(passed, "Batch of 2026 in Bengaluru must PASS");
    }

    @Test
    @DisplayName("Batch 2027 INVALID: Rubrik Winter Intern with 2027 graduates -> FAIL")
    void testBatch2027_RubrikWinterIntern() {
        boolean passed = evaluateJobWithTitle(
                "Software Engineer - Winter Intern",
                "Bangalore, India",
                "- CGPA 8 and above\n- 2027 graduates of Circuital branches only\n- Available from January 2027 to May 2027 in Bangalore"
        );
        assertFalse(passed, "2027 graduates must FAIL (batch above 2026 strictly excluded)");
    }

    @Test
    @DisplayName("Batch 2027 INVALID: Graduating in 2027 -> FAIL")
    void testBatch2027_GraduatingIn2027() {
        boolean passed = evaluateJobWithTitle("Software Engineer", "Hyderabad, India", "Graduating in 2027. Summer internship program.");
        assertFalse(passed, "Graduating in 2027 must FAIL (batch above 2026 strictly excluded)");
    }

    @Test
    @DisplayName("Batch 2028 INVALID: 2028 batch candidates -> FAIL")
    void testBatch2028_BatchCandidates() {
        boolean passed = evaluateJobWithTitle("Trainee Developer", "Hyderabad, India", "2028 batch candidates only.");
        assertFalse(passed, "2028 batch must FAIL (batch above 2026 strictly excluded)");
    }

    // ==========================================
    // SECTION 8: COUNTRY NORMALIZER TESTS
    // ==========================================
    @Test
    @DisplayName("CountryNormalizer correctly normalizes India variations")
    void testCountryNormalizer() {
        String[] validIndiaInputs = {"India", "INDIA", "IN", "IN - India", "India, IN", "Remote - India", "India - Remote"};
        for (String input : validIndiaInputs) {
            var norm = countryNormalizer.normalize(input);
            assertTrue(norm.isIndia(), "Failed for input: " + input);
            assertEquals("INDIA", norm.normalizedCountry(), "Normalized country must be INDIA for: " + input);
            assertEquals("IN", norm.countryCode(), "Country code must be IN for: " + input);
            assertEquals("India", norm.countryName(), "Country name must be India for: " + input);
        }

        String[] foreignInputs = {"USA", "UK", "London", "Singapore", "Canada"};
        for (String input : foreignInputs) {
            var norm = countryNormalizer.normalize(input);
            assertFalse(norm.isIndia(), "Foreign input must not be India: " + input);
            assertNotEquals("INDIA", norm.normalizedCountry());
        }
    }

    // ==========================================
    // SECTION 9: INDIA JOB LOCATION VALIDATOR TESTS
    // ==========================================
    @Test
    @DisplayName("IndiaJobLocationValidator classifies locations correctly")
    void testIndiaJobLocationValidator() {
        assertEquals(LocationClassification.INDIA, locationValidator.validate("Hyderabad, India", "SE", ""));
        assertEquals(LocationClassification.INDIA, locationValidator.validate("Bengaluru, Karnataka", "SE", ""));
        assertEquals(LocationClassification.INDIA, locationValidator.validate("Remote - India", "SE", ""));

        assertEquals(LocationClassification.NON_INDIA, locationValidator.validate("New York, USA", "SE", ""));
        assertEquals(LocationClassification.NON_INDIA, locationValidator.validate("London, UK", "SE", ""));
        assertEquals(LocationClassification.NON_INDIA, locationValidator.validate("Singapore", "SE", ""));
        assertEquals(LocationClassification.NON_INDIA, locationValidator.validate("Remote - Global", "SE", ""));

        assertEquals(LocationClassification.UNKNOWN, locationValidator.validate("Remote", "SE", ""));
        assertEquals(LocationClassification.UNKNOWN, locationValidator.validate("Worldwide", "SE", ""));
        assertEquals(LocationClassification.UNKNOWN, locationValidator.validate(null, "SE", ""));
    }
}
