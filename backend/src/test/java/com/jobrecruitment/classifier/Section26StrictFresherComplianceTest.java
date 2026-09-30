package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.EligibilityStatus;
import com.jobrecruitment.entity.LocationClassification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Automated tests for Section 26 of MASTER RECTIFICATION PROMPT:
 * Items A through O, title traps, remote validation, and eligibility checks.
 */
class Section26StrictFresherComplianceTest {

    private JobLocationParser locationParser;
    private ExperienceRequirementParser experienceParser;
    private CountryNormalizer countryNormalizer;
    private IndiaJobLocationValidator locationValidator;
    private FresherJobEligibilityService eligibilityService;

    @BeforeEach
    void setUp() {
        locationParser = new JobLocationParser();
        countryNormalizer = new CountryNormalizer();
        locationValidator = new IndiaJobLocationValidator(locationParser, countryNormalizer);
        experienceParser = new ExperienceRequirementParser();
        eligibilityService = new FresherJobEligibilityService(locationParser, experienceParser, locationValidator, countryNormalizer);
    }

    // ==========================================
    // SECTION 26: EXPERIENCE REQUIREMENTS (A - H)
    // ==========================================

    @Test
    @DisplayName("A. '0 years experience' -> PASS")
    void testA_ZeroYearsExperience() {
        var res = experienceParser.parse("Software Engineer", "Requirements: 0 years experience in Java programming.");
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, res.experienceClassification());
        assertEquals(0, res.minimumExperienceYears());
    }

    @Test
    @DisplayName("B. 'No experience required' -> PASS")
    void testB_NoExperienceRequired() {
        var res = experienceParser.parse("Junior Web Developer", "Entry level position. No experience required.");
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, res.experienceClassification());
        assertEquals(0, res.minimumExperienceYears());
    }

    @Test
    @DisplayName("C. 'Freshers can apply' -> PASS")
    void testC_FreshersCanApply() {
        var res = experienceParser.parse("Trainee Engineer", "Freshers can apply for our graduate training program.");
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, res.experienceClassification());
        assertEquals(0, res.minimumExperienceYears());
    }

    @Test
    @DisplayName("D. '0-1 years' -> PASS")
    void testD_ZeroTo1Years() {
        var res = experienceParser.parse("Java Developer", "Experience required: 0-1 years of experience in Java / Spring Boot.");
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, res.experienceClassification());
        assertEquals(0, res.minimumExperienceYears());
    }

    @Test
    @DisplayName("E. '1-2 years' -> FAIL")
    void testE_OneToTwoYears() {
        var res = experienceParser.parse("Software Engineer", "Experience required: 1-2 years in backend systems.");
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, res.experienceClassification());
        assertTrue(res.minimumExperienceYears() >= 1);
    }

    @Test
    @DisplayName("F. '2+ years' -> FAIL")
    void testF_TwoPlusYears() {
        var res = experienceParser.parse("Frontend Developer", "Must have 2+ years of experience in React.");
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, res.experienceClassification());
        assertEquals(2, res.minimumExperienceYears());
    }

    @Test
    @DisplayName("G. '3 years' -> FAIL")
    void testG_ThreeYears() {
        var res = experienceParser.parse("DevOps Engineer", "Minimum 3 years experience with Docker and Kubernetes.");
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, res.experienceClassification());
        assertEquals(3, res.minimumExperienceYears());
    }

    @Test
    @DisplayName("H. Unknown experience -> FAIL (UNKNOWN)")
    void testH_UnknownExperience() {
        var res = experienceParser.parse("Software Developer", "Looking for talented engineers to build great products.");
        assertEquals(EligibilityStatus.UNKNOWN, res.experienceClassification());
        assertNull(res.minimumExperienceYears());
    }

    // ==========================================
    // SECTION 26: LOCATION REQUIREMENTS (I - O)
    // ==========================================

    @Test
    @DisplayName("I. 'Hyderabad, India' -> PASS")
    void testI_HyderabadIndia() {
        var res = locationParser.parse("Hyderabad, India", "Software Engineer", "");
        assertEquals(LocationClassification.INDIA, res.classification());
        assertEquals("INDIA", res.country());
        assertEquals("IN", res.countryCode());
        assertEquals("India", res.countryName());
        assertEquals("Hyderabad", res.city());
        assertEquals("Telangana", res.state());
    }

    @Test
    @DisplayName("J. 'Bengaluru, India' -> PASS")
    void testJ_BengaluruIndia() {
        var res = locationParser.parse("Bengaluru, Karnataka, India", "Java Engineer", "");
        assertEquals(LocationClassification.INDIA, res.classification());
        assertEquals("INDIA", res.country());
        assertEquals("Bengaluru", res.city());
    }

    @Test
    @DisplayName("K. 'Remote - India' -> PASS")
    void testK_RemoteIndia() {
        var res = locationParser.parse("Remote - India", "Full Stack Developer", "");
        assertEquals(LocationClassification.INDIA, res.classification());
        assertEquals("INDIA", res.country());
        assertTrue(res.isRemote());
    }

    @Test
    @DisplayName("L. 'New York, USA' -> FAIL (NON_INDIA)")
    void testL_NewYorkUSA() {
        var res = locationParser.parse("New York, USA", "Software Engineer", "");
        assertEquals(LocationClassification.NON_INDIA, res.classification());
        assertNotEquals("India", res.country());
    }

    @Test
    @DisplayName("M. 'London, UK' -> FAIL (NON_INDIA)")
    void testM_LondonUK() {
        var res = locationParser.parse("London, UK", "Graduate Engineer", "");
        assertEquals(LocationClassification.NON_INDIA, res.classification());
    }

    @Test
    @DisplayName("N. 'Singapore' -> FAIL (NON_INDIA)")
    void testN_Singapore() {
        var res = locationParser.parse("Singapore", "Backend Developer", "");
        assertEquals(LocationClassification.NON_INDIA, res.classification());
    }

    @Test
    @DisplayName("O. 'Remote - USA' -> FAIL (NON_INDIA)")
    void testO_RemoteUSA() {
        var res = locationParser.parse("Remote - USA", "Software Engineer", "");
        assertEquals(LocationClassification.NON_INDIA, res.classification());
    }

    // ==========================================
    // ADDITIONAL SECTION 26 / 7 TRAP TESTS
    // ==========================================

    @Test
    @DisplayName("Title Trap 1: 'Associate Software Engineer - 2 years experience' -> REJECT")
    void testTitleTrap_Associate2Years() {
        var decision = eligibilityService.evaluateEligibility(
                "Associate Software Engineer",
                "Requires minimum 2 years experience with Java.",
                "Hyderabad, India"
        );
        assertFalse(decision.isEligible());
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, decision.experience().experienceClassification());
    }

    @Test
    @DisplayName("Title Trap 2: 'Junior Java Developer - 1 year experience' -> REJECT")
    void testTitleTrap_Junior1Year() {
        var decision = eligibilityService.evaluateEligibility(
                "Junior Java Developer",
                "Must have 1+ years experience in software development.",
                "Bengaluru, India"
        );
        assertFalse(decision.isEligible());
        assertEquals(EligibilityStatus.NOT_ELIGIBLE, decision.experience().experienceClassification());
    }

    @Test
    @DisplayName("Ambiguous Remote without India -> REJECT (UNKNOWN)")
    void testAmbiguousRemoteWithoutIndia() {
        var decision = eligibilityService.evaluateEligibility(
                "Software Engineer",
                "No experience required. Freshers eligible.",
                "Remote"
        );
        // "Remote" alone without India confirmation must be rejected under strict policy
        assertFalse(decision.isEligible());
        assertEquals(LocationClassification.UNKNOWN, decision.location().classification());
    }

    @Test
    @DisplayName("Valid India Fresher: 0 years + Bengaluru, India -> ACCEPT")
    void testValidIndiaFresher() {
        var decision = eligibilityService.evaluateEligibility(
                "Software Engineer",
                "Fresh graduates welcome. 0 years experience required.",
                "Bengaluru, Karnataka, India"
        );
        assertTrue(decision.isEligible());
        assertEquals(LocationClassification.INDIA, decision.location().classification());
        assertEquals(EligibilityStatus.ELIGIBLE_ZERO_YEAR, decision.experience().experienceClassification());
    }
}
