package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.ExperienceLevel;

public class FresherClassificationResult {
    private final ExperienceLevel experienceLevel;
    private final boolean isFresher;
    private final int confidence;
    private final String reason;

    public FresherClassificationResult(ExperienceLevel experienceLevel, boolean isFresher, int confidence, String reason) {
        this.experienceLevel = experienceLevel;
        this.isFresher = isFresher;
        this.confidence = confidence;
        this.reason = reason;
    }

    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public boolean isFresher() { return isFresher; }
    public int getConfidence() { return confidence; }
    public String getReason() { return reason; }

    @Override
    public String toString() {
        return "Classification{" +
                "level=" + experienceLevel +
                ", fresher=" + isFresher +
                ", confidence=" + confidence +
                ", reason='" + reason + '\'' +
                '}';
    }
}
