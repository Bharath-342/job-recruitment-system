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
        var decision = eligibilityService.evaluateEligibility("Software Engineer", expText, location);
        if (!decision.isEligible()) {
            return false;
        }

        // Also test the central entity checker (Section 12)
        AggregatedJob job = new AggregatedJob();
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
