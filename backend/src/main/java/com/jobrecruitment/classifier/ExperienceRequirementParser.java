package com.jobrecruitment.classifier;

import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ExperienceRequirementParser {

    // Senior / Management patterns (Immediate Exclusion)
    private static final Pattern SENIOR_TITLE_PATTERN = Pattern.compile(
            "\\b(senior|sr\\.?|lead|principal|staff|architect|director|head|vp|vice president|manager|chief|mgr)\\b",
            Pattern.CASE_INSENSITIVE);

    // Explicit high / mandatory prior experience patterns (1+ years, 2+ years, etc.)
    private static final Pattern MANDATORY_EXPERIENCE_PATTERN = Pattern.compile(
            "\\b(?:minimum|at least|requires?|must have)\\s+([1-9]\\d*)\\s*(?:\\+|-|to)?\\s*\\d*\\s*(?:years?|yrs?)\\b|" +
            "\\b([1-9]\\d*)\\+?\\s*(?:-|to)?\\s*\\d*\\s*(?:years?|yrs?)\\b",
            Pattern.CASE_INSENSITIVE);

    // Explicit 0-year / Fresher patterns
    private static final Pattern ZERO_YEAR_EXPLICIT_PATTERN = Pattern.compile(
            "\\b(?:0\\s*(?:-|to)?\\s*[12]?\\s*(?:years?|yrs?)|0\\s*(?:years?|yrs?)|no\\s+(?:prior\\s+)?experience\\s+(?:required|needed|necessary)|freshers?\\s+(?:eligible|welcome|encouraged)|fresh\\s+graduates?|recent\\s+graduates?|university\\s+graduates?)\\b",
            Pattern.CASE_INSENSITIVE);

    // Trainee / Intern / Graduate Engineer role patterns
    private static final Pattern FRESHER_ROLE_PATTERN = Pattern.compile(
            "\\b(?:intern|internship|trainee|apprentice|apprenticeship|graduate\\s+software\\s+engineer|graduate\\s+engineer|software\\s+engineer\\s+intern|developer\\s+intern)\\b",
            Pattern.CASE_INSENSITIVE);

    public record ParsedExperience(
            Integer minimumExperienceYears,
            boolean isExplicitZeroYear,
            boolean hasMandatoryPriorExperience,
            boolean isSeniorOrLead,
            String details
    ) {}

    public ParsedExperience parse(String title, String description) {
        String safeTitle = title != null ? title : "";
        String safeDesc = description != null ? description : "";
        String fullText = safeTitle + " " + safeDesc;

        boolean isSenior = SENIOR_TITLE_PATTERN.matcher(safeTitle).find();

        // Mask 0-year ranges (e.g. 0-1 years, 0 to 1 years) so they don't falsely trigger the 1+ year pattern
        String textForMandatoryCheck = fullText.replaceAll("(?i)\\b0\\s*(?:-|to)\\s*[12]?\\s*(?:years?|yrs?)\\b", "__ZERO_EXP_RANGE__");

        // Check for mandatory prior experience
        Matcher expMatcher = MANDATORY_EXPERIENCE_PATTERN.matcher(textForMandatoryCheck);
        boolean hasMandatoryPriorExp = false;
        int maxPriorYearsDetected = 0;

        while (expMatcher.find()) {
            String g1 = expMatcher.group(1);
            String g2 = expMatcher.group(2);
            String valStr = g1 != null ? g1 : g2;
            if (valStr != null) {
                try {
                    int yrs = Integer.parseInt(valStr);
                    if (yrs >= 1) {
                        hasMandatoryPriorExp = true;
                        maxPriorYearsDetected = Math.max(maxPriorYearsDetected, yrs);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        // Check for explicit zero-year indicators
        boolean explicitZero = ZERO_YEAR_EXPLICIT_PATTERN.matcher(fullText).find();
        boolean fresherRole = FRESHER_ROLE_PATTERN.matcher(safeTitle).find() || FRESHER_ROLE_PATTERN.matcher(safeDesc).find();

        // STRICT CONFLICT RULE: If ANY mandatory prior experience >= 1 is found, it overrides fresher keywords!
        if (hasMandatoryPriorExp || isSenior) {
            int minYears = maxPriorYearsDetected > 0 ? maxPriorYearsDetected : (isSenior ? 5 : 1);
            String details = isSenior ? "Senior/Lead role excluded" : "Requires " + minYears + "+ years prior experience";
            return new ParsedExperience(minYears, false, true, isSenior, details);
        }

        // Zero-year eligibility confirmed
        if (explicitZero || fresherRole) {
            String details = explicitZero ? "0 years / Fresher explicitly eligible" : "Entry-level / Trainee / Intern opening";
            return new ParsedExperience(0, true, false, false, details);
        }

        // AMBIGUOUS RULE: Experience is missing or cannot be verified -> EXCLUDE
        return new ParsedExperience(null, false, false, false, "Experience requirement not explicitly stated (Ambiguous - Excluded)");
    }
}
