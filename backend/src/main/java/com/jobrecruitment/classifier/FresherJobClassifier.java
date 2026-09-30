package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.ExperienceLevel;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class FresherJobClassifier {

    // Negative seniority patterns (Title & Description)
    private static final Pattern SENIOR_TITLE_PATTERN = Pattern.compile(
            "\\b(senior|sr\\.?|lead|principal|staff|architect|director|head|vp|vice president|manager|chief|mgr)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern HIGH_EXP_PATTERN = Pattern.compile(
            "\\b([3-9]|1[0-9]|20)\\+?\\s*(?:to|-)?\\s*\\d*\\s*(?:years?|yrs?)(?:\\s+of)?\\s+(?:experience|exp)\\b|\\b(?:minimum|at least)\\s+([3-9]|1[0-9]|20)\\s*(?:years?|yrs?)\\b",
            Pattern.CASE_INSENSITIVE);

    // Positive fresher patterns
    private static final Pattern EXPLICIT_FRESHER_PATTERN = Pattern.compile(
            "\\b(fresher|freshers|fresher's|fresh graduate|fresh graduates|new grad|new graduate|new graduates|recent graduate|recent graduates)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern GRADUATE_PATTERN = Pattern.compile(
            "\\b(graduate|university graduate|campus recruit|campus hiring|graduate engineer|graduate software engineer)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern ZERO_ONE_EXP_PATTERN = Pattern.compile(
            "\\b(0\\s*(?:-|to)\\s*1\\s*(?:years?|yrs?)|0\\s*(?:-|to)\\s*2\\s*(?:years?|yrs?)|0\\s*(?:years?|yrs?)|no\\s+experience\\s+required|freshers\\s+welcome)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern ENTRY_LEVEL_PATTERN = Pattern.compile(
            "\\b(entry[ -]?level|entrylevel)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern JUNIOR_PATTERN = Pattern.compile(
            "\\b(junior|jr\\.?)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern TRAINEE_PATTERN = Pattern.compile(
            "\\b(trainee|apprentice|apprenticeship|intern|internship)\\b",
            Pattern.CASE_INSENSITIVE);

    private static final Pattern ASSOCIATE_PATTERN = Pattern.compile(
            "\\b(associate|assoc\\.?)\\b",
            Pattern.CASE_INSENSITIVE);

    public FresherClassificationResult classify(String title, String description) {
        String safeTitle = title != null ? title : "";
        String safeDesc = description != null ? description : "";
        String fullText = safeTitle + " " + safeDesc;

        int score = 0;
        StringBuilder reasons = new StringBuilder();

        // 1. Check strong negative signals first
        boolean isSeniorTitle = SENIOR_TITLE_PATTERN.matcher(safeTitle).find();
        if (isSeniorTitle) {
            score -= 50;
            reasons.append("Senior title detected; ");
        }

        Matcher highExpMatcher = HIGH_EXP_PATTERN.matcher(fullText);
        boolean hasHighExp = highExpMatcher.find();
        if (hasHighExp) {
            score -= 50;
            reasons.append("High experience requirement found (").append(highExpMatcher.group()).append("); ");
        }

        // Hard exclusion: If explicitly senior or 3+ years required, it CANNOT be a fresher job
        if (isSeniorTitle || hasHighExp) {
            ExperienceLevel level = isSeniorTitle ? ExperienceLevel.SENIOR : ExperienceLevel.EXPERIENCED;
            return new FresherClassificationResult(level, false, Math.min(score, -10), reasons.toString().trim());
        }

        // 2. Positive signals
        boolean hasExplicitFresher = EXPLICIT_FRESHER_PATTERN.matcher(fullText).find();
        if (hasExplicitFresher) {
            score += 30;
            reasons.append("Explicit fresher indicator (+30); ");
        }

        boolean hasGraduate = GRADUATE_PATTERN.matcher(fullText).find();
        if (hasGraduate) {
            score += 30;
            reasons.append("Graduate keyword (+30); ");
        }

        boolean hasZeroExp = ZERO_ONE_EXP_PATTERN.matcher(fullText).find();
        if (hasZeroExp) {
            score += 25;
            reasons.append("0-1 or 0-2 years exp (+25); ");
        }

        boolean hasEntryLevel = ENTRY_LEVEL_PATTERN.matcher(fullText).find();
        if (hasEntryLevel) {
            score += 20;
            reasons.append("Entry-level keyword (+20); ");
        }

        boolean hasJunior = JUNIOR_PATTERN.matcher(safeTitle).find() || JUNIOR_PATTERN.matcher(safeDesc).find();
        if (hasJunior) {
            score += 15;
            reasons.append("Junior role indicator (+15); ");
        }

        boolean hasTrainee = TRAINEE_PATTERN.matcher(fullText).find();
        if (hasTrainee) {
            score += 15;
            reasons.append("Trainee/intern indicator (+15); ");
        }

        boolean hasAssociate = ASSOCIATE_PATTERN.matcher(safeTitle).find();
        if (hasAssociate) {
            score += 15;
            reasons.append("Associate title indicator (+15); ");
        }

        // 3. Determine Level and isFresher
        // Threshold for fresher: >= 15
        if (score >= 15) {
            ExperienceLevel level;
            if (hasExplicitFresher || hasZeroExp) {
                level = ExperienceLevel.FRESHER;
            } else if (hasGraduate) {
                level = ExperienceLevel.FRESHER;
            } else if (hasEntryLevel || hasAssociate) {
                level = ExperienceLevel.ENTRY_LEVEL;
            } else if (hasJunior) {
                level = ExperienceLevel.JUNIOR;
            } else {
                level = ExperienceLevel.FRESHER;
            }
            return new FresherClassificationResult(level, true, score, reasons.toString().trim());
        }

        // If score < 15 and unclear
        return new FresherClassificationResult(ExperienceLevel.UNKNOWN, false, score, "Unclear experience requirements");
    }
}
