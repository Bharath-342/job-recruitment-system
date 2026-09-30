package com.jobrecruitment.classifier;

import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Dedicated Country Normalizer per Section 8 of Master Rectification.
 * Normalizes values such as:
 * - India
 * - INDIA
 * - IN
 * - IN - India
 * - India, IN
 * - India - Remote
 * - Remote - India
 * into: INDIA
 *
 * Foreign locations are NEVER mapped to INDIA.
 * Stores / provides countryCode = "IN", countryName = "India".
 */
@Component
public class CountryNormalizer {

    public static final String NORMALIZED_INDIA = "INDIA";
    public static final String COUNTRY_CODE_INDIA = "IN";
    public static final String COUNTRY_NAME_INDIA = "India";

    private static final Pattern INDIA_EXACT_PATTERN = Pattern.compile(
            "^(?:india|in|in\\s*-\\s*india|india,\\s*in|in,\\s*india)$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern INDIA_REMOTE_PATTERN = Pattern.compile(
            "^(?:remote\\s*-\\s*india|india\\s*-\\s*remote|remote\\s*\\(india\\)|india\\s*\\(remote\\)|remote,\\s*india|india,\\s*remote)$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern NON_INDIA_HINT = Pattern.compile(
            "\\b(usa|united states|uk|united kingdom|canada|australia|germany|france|singapore|uae|dubai|netherlands|ireland|switzerland|japan|china|brazil|spain|italy)\\b",
            Pattern.CASE_INSENSITIVE
    );

    public record NormalizedCountry(
            String normalizedCountry,
            String countryCode,
            String countryName,
            boolean isIndia
    ) {}

    public NormalizedCountry normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return new NormalizedCountry("UNKNOWN", null, null, false);
        }

        String trimmed = raw.trim();

        // 1. Check exact India patterns
        if (INDIA_EXACT_PATTERN.matcher(trimmed).matches() ||
            INDIA_REMOTE_PATTERN.matcher(trimmed).matches()) {
            return new NormalizedCountry(NORMALIZED_INDIA, COUNTRY_CODE_INDIA, COUNTRY_NAME_INDIA, true);
        }

        // 2. Reject if any foreign country is explicitly present
        if (NON_INDIA_HINT.matcher(trimmed).find()) {
            return new NormalizedCountry("NON_INDIA", null, trimmed, false);
        }

        // 3. Check boundary India word
        if (Pattern.compile("\\bindia\\b", Pattern.CASE_INSENSITIVE).matcher(trimmed).find()) {
            return new NormalizedCountry(NORMALIZED_INDIA, COUNTRY_CODE_INDIA, COUNTRY_NAME_INDIA, true);
        }

        return new NormalizedCountry("UNKNOWN", null, trimmed, false);
    }
}
