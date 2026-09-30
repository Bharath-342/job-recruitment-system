package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.EligibilityStatus;
import com.jobrecruitment.entity.LocationClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Central Eligibility Service enforcing the core product rule (Section 12):
 *
 * isEligibleForIndianFreshers(job):
 *     if job.country != "INDIA":
 *         return false
 *     if job.locationClassification != "INDIA":
 *         return false
 *     if job.experienceClassification != "ELIGIBLE_ZERO_YEAR":
 *         return false
 *     if job.isActive != true:
 *         return false
 *     return true
 *
 * All other jobs (Foreign, Experienced, Unknown) are strictly rejected.
 */
@Service
public class FresherJobEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(FresherJobEligibilityService.class);

    private final JobLocationParser locationParser;
    private final ExperienceRequirementParser experienceParser;
    private final IndiaJobLocationValidator locationValidator;
    private final CountryNormalizer countryNormalizer;
    private final JavaRelevanceClassifier relevanceClassifier;

    public FresherJobEligibilityService(JobLocationParser locationParser,
                                        ExperienceRequirementParser experienceParser) {
        this(locationParser, experienceParser,
             new IndiaJobLocationValidator(locationParser, new CountryNormalizer()),
             new CountryNormalizer(),
             new JavaRelevanceClassifier());
    }

    public FresherJobEligibilityService(JobLocationParser locationParser,
                                        ExperienceRequirementParser experienceParser,
                                        IndiaJobLocationValidator locationValidator,
                                        CountryNormalizer countryNormalizer) {
        this(locationParser, experienceParser, locationValidator, countryNormalizer, new JavaRelevanceClassifier());
    }

    @org.springframework.beans.factory.annotation.Autowired
    public FresherJobEligibilityService(JobLocationParser locationParser,
                                        ExperienceRequirementParser experienceParser,
                                        IndiaJobLocationValidator locationValidator,
                                        CountryNormalizer countryNormalizer,
                                        JavaRelevanceClassifier relevanceClassifier) {
        this.locationParser = locationParser;
        this.experienceParser = experienceParser;
        this.locationValidator = locationValidator;
        this.countryNormalizer = countryNormalizer;
        this.relevanceClassifier = relevanceClassifier;
    }

    public record FresherEligibilityDecision(
            boolean isEligible,
            JobLocationParser.ParsedLocation location,
            ExperienceRequirementParser.ParsedExperience experience,
            JavaRelevanceClassifier.JavaRelevanceResult relevance,
            String rejectionReason
    ) {}

    /**
     * Central eligibility check evaluated against an AggregatedJob entity per Section 12.
     */
    public boolean isEligibleForIndianFreshers(AggregatedJob job) {
        if (job == null) {
            return false;
        }

        // 1. Country == INDIA
        if (job.getCountry() == null || !CountryNormalizer.NORMALIZED_INDIA.equalsIgnoreCase(job.getCountry().trim())) {
            log.debug("isEligibleForIndianFreshers check failed: Country [{}] != INDIA", job.getCountry());
            return false;
        }

        // 2. LocationClassification == INDIA
        if (job.getLocationClassification() != LocationClassification.INDIA) {
            log.debug("isEligibleForIndianFreshers check failed: LocationClassification [{}] != INDIA", job.getLocationClassification());
            return false;
        }

        // 3. ExperienceClassification == ELIGIBLE_ZERO_YEAR (minimumExperienceYears == 0)
        if (job.getEligibilityStatus() != EligibilityStatus.ELIGIBLE_ZERO_YEAR) {
            log.debug("isEligibleForIndianFreshers check failed: EligibilityStatus [{}] != ELIGIBLE_ZERO_YEAR", job.getEligibilityStatus());
            return false;
        }

        if (job.getMinimumExperienceYears() != null && job.getMinimumExperienceYears() > 0) {
            log.debug("isEligibleForIndianFreshers check failed: minimumExperienceYears [{}] > 0", job.getMinimumExperienceYears());
            return false;
        }

        // 4. Must be an IT / Software role (Reject Non-IT per Section 2)
        if (job.getRoleCategory() == com.jobrecruitment.entity.RoleCategory.OTHER) {
            log.debug("isEligibleForIndianFreshers check failed: RoleCategory is OTHER (Non-IT)");
            return false;
        }

        // 5. isActive == true
        if (!job.isActive()) {
            log.debug("isEligibleForIndianFreshers check failed: isActive is false");
            return false;
        }

        return true;
    }

    /**
     * Evaluates raw job attributes during ingestion.
     */
    public FresherEligibilityDecision evaluateEligibility(String title, String description, String rawLocation) {
        // 1. Validate Location (India Only)
        JobLocationParser.ParsedLocation parsedLoc = locationParser.parse(rawLocation, title, description);
        if (parsedLoc.classification() != LocationClassification.INDIA) {
            String reason = parsedLoc.classification() == LocationClassification.NON_INDIA
                    ? "REJECTED_FOREIGN_COUNTRY (" + parsedLoc.details() + ")"
                    : "REJECTED_UNKNOWN_COUNTRY (" + (rawLocation != null ? rawLocation : "blank") + ")";
            log.info("Location check failed: {} - Location: '{}'", reason, rawLocation);
            return new FresherEligibilityDecision(false, parsedLoc, null, null, reason);
        }

        // 2. Validate Experience (Strict 0-Year Only)
        ExperienceRequirementParser.ParsedExperience parsedExp = experienceParser.parse(title, description);
        if (parsedExp.experienceClassification() != EligibilityStatus.ELIGIBLE_ZERO_YEAR) {
            String reason = parsedExp.experienceClassification() == EligibilityStatus.NOT_ELIGIBLE
                    ? "REJECTED_EXPERIENCE_REQUIRED (" + parsedExp.details() + ")"
                    : "REJECTED_UNKNOWN_EXPERIENCE (Experience unverifiable in source text)";
            log.info("Experience check failed: {} - Title: '{}'", reason, title);
            return new FresherEligibilityDecision(false, parsedLoc, parsedExp, null, reason);
        }

        // 3. Validate IT / Software Role Relevance (Strict IT-Only per Section 2 & 3)
        JavaRelevanceClassifier.JavaRelevanceResult relevance = relevanceClassifier.classify(title, description);
        if (!relevance.isItSoftware()) {
            String reason = "REJECTED_NON_IT_ROLE (" + relevance.details() + ")";
            log.info("IT/Software role check failed: {} - Title: '{}'", reason, title);
            return new FresherEligibilityDecision(false, parsedLoc, parsedExp, relevance, reason);
        }

        // All checks passed: India + Strict 0-Year + IT/Software
        return new FresherEligibilityDecision(true, parsedLoc, parsedExp, relevance, null);
    }

    public IndiaJobLocationValidator getLocationValidator() {
        return locationValidator;
    }

    public CountryNormalizer getCountryNormalizer() {
        return countryNormalizer;
    }
}
