package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.EligibilityStatus;
import com.jobrecruitment.entity.LocationClassification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Central Eligibility Service enforcing the core product rule:
 * COUNTRY == India (LocationClassification.INDIA)
 * AND
 * EXPERIENCE == 0 Years (EligibilityStatus.ELIGIBLE_ZERO_YEAR)
 *
 * All other jobs (Foreign, Experienced, Unknown) are strictly rejected.
 */
@Service
public class FresherJobEligibilityService {

    private static final Logger log = LoggerFactory.getLogger(FresherJobEligibilityService.class);

    private final JobLocationParser locationParser;
    private final ExperienceRequirementParser experienceParser;

    public FresherJobEligibilityService(JobLocationParser locationParser,
                                        ExperienceRequirementParser experienceParser) {
        this.locationParser = locationParser;
        this.experienceParser = experienceParser;
    }

    public record FresherEligibilityDecision(
            boolean isEligible,
            JobLocationParser.ParsedLocation location,
            ExperienceRequirementParser.ParsedExperience experience,
            String rejectionReason
    ) {}

    public FresherEligibilityDecision evaluateEligibility(String title, String description, String rawLocation) {
        // 1. Validate Location (India Only)
        JobLocationParser.ParsedLocation parsedLoc = locationParser.parse(rawLocation, title, description);
        if (parsedLoc.classification() != LocationClassification.INDIA) {
            String reason = parsedLoc.classification() == LocationClassification.NON_INDIA
                    ? "Rejected: Foreign location (" + parsedLoc.details() + ")"
                    : "Rejected: Ambiguous/Unknown location (" + (rawLocation != null ? rawLocation : "blank") + ")";
            return new FresherEligibilityDecision(false, parsedLoc, null, reason);
        }

        // 2. Validate Experience (Strict 0-Year Only)
        ExperienceRequirementParser.ParsedExperience parsedExp = experienceParser.parse(title, description);
        if (parsedExp.experienceClassification() != EligibilityStatus.ELIGIBLE_ZERO_YEAR) {
            String reason = parsedExp.experienceClassification() == EligibilityStatus.NOT_ELIGIBLE
                    ? "Rejected: Requires prior experience (" + parsedExp.details() + ")"
                    : "Rejected: Experience requirement unknown/unverifiable in source";
            return new FresherEligibilityDecision(false, parsedLoc, parsedExp, reason);
        }

        // Both checks passed: India + Strict 0-Year
        return new FresherEligibilityDecision(true, parsedLoc, parsedExp, null);
    }
}
