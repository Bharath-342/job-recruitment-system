package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.EligibilityStatus;
import org.springframework.stereotype.Service;

@Service
public class FresherEligibilityService {

    private final ExperienceRequirementParser experienceParser;

    public FresherEligibilityService(ExperienceRequirementParser experienceParser) {
        this.experienceParser = experienceParser;
    }

    public record EligibilityResult(
            EligibilityStatus status,
            Integer minimumExperienceYears,
            String explanation
    ) {}

    public EligibilityResult determineEligibility(String title, String description) {
        ExperienceRequirementParser.ParsedExperience parsed = experienceParser.parse(title, description);

        // 1. Mandatory prior experience or senior role
        if (parsed.hasMandatoryPriorExperience() || parsed.isSeniorOrLead()) {
            return new EligibilityResult(
                    EligibilityStatus.NOT_ELIGIBLE,
                    parsed.minimumExperienceYears(),
                    parsed.details()
            );
        }

        // 2. Explicit 0-year eligibility verified
        if (parsed.minimumExperienceYears() != null && parsed.minimumExperienceYears() == 0) {
            return new EligibilityResult(
                    EligibilityStatus.ELIGIBLE_ZERO_YEAR,
                    0,
                    parsed.details()
            );
        }

        // 3. Ambiguous or missing experience requirement -> EXCLUDE
        return new EligibilityResult(
                EligibilityStatus.UNKNOWN,
                null,
                "Ambiguous experience requirement: excluded under strict fresher-only policy"
        );
    }
}
