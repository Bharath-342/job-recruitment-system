package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.EligibilityStatus;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dedicated parser for extracting and classifying job experience requirements.
 * STRICT POLICY:
 * - Only genuine ELIGIBLE_ZERO_YEAR is admitted.
 * - Any job requiring 1+ years minimum prior experience is NOT_ELIGIBLE.
 * - Senior / Lead roles are NOT_ELIGIBLE.
 * - Missing / unverified experience is UNKNOWN (which is rejected from Fresher feeds).
 * - Job titles alone are NEVER trusted without source confirmation.
 */
@Component
public class ExperienceRequirementParser {

    public record ParsedExperience(
            Integer minimumExperienceYears,
            Integer maximumExperienceYears,
            String experienceText,
            int experienceConfidence,
            EligibilityStatus experienceClassification,
            String details
    ) {}

    // Senior / Management roles - Immediate NOT_ELIGIBLE
    private static final Pattern SENIOR_ROLE_PATTERN = Pattern.compile(
            "\\b(senior|sr\\.?|lead|principal|staff|architect|director|head|vp|vice president|manager|chief|team lead)\\b",
            Pattern.CASE_INSENSITIVE);

    // Explicit high / mandatory prior experience patterns (1+ years, 1-3 years, 2+ years, etc.)
    private static final Pattern MANDATORY_PRIOR_EXP_PATTERN = Pattern.compile(
            "\\b(?:minimum|at least|requires?|must have|minimum of)\\s+([1-9]\\d*)\\s*(?:\\+|-|to)?\\s*\\d*\\s*(?:years?|yrs?)(?:\\s+of)?(?:\\s+prior)?(?:\\s+professional)?(?:\\s+experience|\\s+exp)?\\b|" +
            "\\b([1-9]\\d*)\\+?\\s*(?:-|to)\\s*([1-9]\\d*)\\s*(?:years?|yrs?)(?:\\s+of)?(?:\\s+prior)?(?:\\s+professional)?(?:\\s+experience|\\s+exp)?\\b|" +
            "\\b([1-9]\\d*)\\s*\\+\\s*(?:years?|yrs?)(?:\\s+of)?(?:\\s+prior)?(?:\\s+professional)?(?:\\s+experience|\\s+exp)?\\b|" +
            "\\b([1-9]\\d*)\\s+(?:years?|yrs?)\\s+(?:of\\s+)?(?:prior\\s+)?(?:professional\\s+)?experience\\s+required\\b|" +
            "\\b(?:experienced candidates only|prior professional experience required|prior industry experience required)\\b",
            Pattern.CASE_INSENSITIVE);

    // Explicit 0-year / Fresher patterns
    private static final Pattern ZERO_YEAR_EXPLICIT_PATTERN = Pattern.compile(
            "\\b(?:0\\s*(?:years?|yrs?)(?:\\s+of)?(?:\\s+experience|\\s+exp)?|" +
            "0\\s*(?:-|to)\\s*1\\s*(?:years?|yrs?)(?:\\s+of)?(?:\\s+experience|\\s+exp)?|" +
            "0\\s*(?:-|to)\\s*2\\s*(?:years?|yrs?)(?:\\s+of)?(?:\\s+experience|\\s+exp)?|" +
            "candidates with 0 years experience|" +
            "no\\s+(?:prior\\s+)?experience\\s+(?:required|needed|necessary)|" +
            "graduates with no experience|" +
            "entry[ -]?level\\s+position\\s+with\\s+no\\s+experience\\s+required|" +
            "freshers?\\s+(?:can\\s+apply|eligible|welcome|encouraged|only)|" +
            "fresh\\s+graduates?|recent\\s+graduates?|freshers?)\\b",
            Pattern.CASE_INSENSITIVE);

    // Single year pattern check: e.g. "1 year experience", "2 years experience", "3 years"
    private static final Pattern SINGLE_YEAR_EXP_PATTERN = Pattern.compile(
            "\\b([1-9]\\d*)\\s*(?:years?|yrs?)(?:\\s+of)?(?:\\s+experience|\\s+exp)?\\b",
            Pattern.CASE_INSENSITIVE);

    public ParsedExperience parse(String title, String description) {
        String safeTitle = title != null ? title.trim() : "";
        String safeDesc = description != null ? description.trim() : "";
        String fullText = safeTitle + " \n " + safeDesc;

        // 1. Check for Senior / Lead in title -> Hard NOT_ELIGIBLE
        Matcher seniorMatcher = SENIOR_ROLE_PATTERN.matcher(safeTitle);
        if (seniorMatcher.find()) {
            return new ParsedExperience(
                    5,
                    null,
                    seniorMatcher.group(),
                    95,
                    EligibilityStatus.NOT_ELIGIBLE,
                    "Senior / Lead title excluded: " + seniorMatcher.group()
            );
        }

        // Mask 0-year phrases so they do not trigger the 1+ year or single year patterns
        // e.g. "0-1 years", "0 to 1 years", "0 to 2 years", "0 years", "no experience required"
        String maskedText = fullText
                .replaceAll("(?i)\\b0\\s*(?:-|to)\\s*[12]?\\s*(?:years?|yrs?)(?:\\s+of)?(?:\\s+experience)?\\b", "__ZERO_RANGE__")
                .replaceAll("(?i)\\b0\\s*(?:years?|yrs?)\\b", "__ZERO_YEARS__")
                .replaceAll("(?i)\\bno\\s+(?:prior\\s+)?experience\\s+(?:required|needed|necessary)\\b", "__NO_EXP__");

        // 2. Check for Mandatory Prior Experience (1+ years, 2+ years, 1-3 years, etc.)
        Matcher mandatoryMatcher = MANDATORY_PRIOR_EXP_PATTERN.matcher(maskedText);
        if (mandatoryMatcher.find()) {
            String matchedSnippet = mandatoryMatcher.group();
            int minYears = extractMinYearsFromSnippet(matchedSnippet);
            return new ParsedExperience(
                    Math.max(minYears, 1),
                    null,
                    matchedSnippet,
                    95,
                    EligibilityStatus.NOT_ELIGIBLE,
                    "Requires prior professional experience: " + matchedSnippet
            );
        }

        // Also check single year pattern on masked text: e.g. "3 years", "2 years experience"
        Matcher singleMatcher = SINGLE_YEAR_EXP_PATTERN.matcher(maskedText);
        while (singleMatcher.find()) {
            String yearStr = singleMatcher.group(1);
            try {
                int yrs = Integer.parseInt(yearStr);
                if (yrs >= 1) {
                    // Check context around match (to avoid false positive on e.g. "within 1 year of graduation")
                    int start = Math.max(0, singleMatcher.start() - 25);
                    int end = Math.min(maskedText.length(), singleMatcher.end() + 25);
                    String context = maskedText.substring(start, end).toLowerCase();
                    if (!context.contains("graduation") && !context.contains("graduated")) {
                        return new ParsedExperience(
                                yrs,
                                null,
                                singleMatcher.group(),
                                90,
                                EligibilityStatus.NOT_ELIGIBLE,
                                "Requires minimum " + yrs + " years experience: " + singleMatcher.group()
                        );
                    }
                }
            } catch (NumberFormatException ignored) {}
        }

        // 3. Check for Explicit 0-Year / Fresher indicators
        Matcher zeroMatcher = ZERO_YEAR_EXPLICIT_PATTERN.matcher(fullText);
        if (zeroMatcher.find()) {
            String matchedSnippet = zeroMatcher.group();
            Integer maxYears = null;
            if (matchedSnippet.toLowerCase().contains("0-1") || matchedSnippet.toLowerCase().contains("0 to 1")) {
                maxYears = 1;
            } else if (matchedSnippet.toLowerCase().contains("0-2") || matchedSnippet.toLowerCase().contains("0 to 2")) {
                maxYears = 2;
            }

            return new ParsedExperience(
                    0,
                    maxYears,
                    matchedSnippet,
                    95,
                    EligibilityStatus.ELIGIBLE_ZERO_YEAR,
                    "Verified 0-year / Fresher eligibility: " + matchedSnippet
            );
        }

        // 4. Job title checks (Rule 7: DO NOT TRUST JOB TITLES ALONE)
        // If the title contains Intern / Trainee / Graduate / Entry Level, BUT description has NO explicit 0-year evidence:
        // Do NOT classify as ELIGIBLE_ZERO_YEAR! Must classify as UNKNOWN.
        // UNKNOWN is strictly rejected by the Fresher platform.
        return new ParsedExperience(
                null,
                null,
                null,
                0,
                EligibilityStatus.UNKNOWN,
                "Experience requirement not explicitly stated or verifiable in source (Ambiguous - Excluded)"
        );
    }

    private int extractMinYearsFromSnippet(String snippet) {
        Matcher numMatcher = Pattern.compile("([1-9]\\d*)").matcher(snippet);
        if (numMatcher.find()) {
            try {
                return Integer.parseInt(numMatcher.group(1));
            } catch (NumberFormatException ignored) {}
        }
        return 1;
    }
}
