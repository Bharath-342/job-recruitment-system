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

        return new EligibilityResult(
                parsed.experienceClassification(),
                parsed.minimumExperienceYears(),
                parsed.details()
        );
    }
}
