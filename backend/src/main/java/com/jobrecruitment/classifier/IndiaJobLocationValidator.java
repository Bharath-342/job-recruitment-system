package com.jobrecruitment.classifier;

import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.LocationClassification;
import org.springframework.stereotype.Component;

/**
 * Location Validation Service per Section 9 of Master Rectification.
 * Input: Job / rawLocation / location / title / description
 * Output: LocationClassification: INDIA, NON_INDIA, UNKNOWN
 *
 * Rules:
 * if explicitly India -> INDIA
 * if explicitly another country -> NON_INDIA
 * if unclear -> UNKNOWN
 *
 * Only INDIA is allowed. NON_INDIA and UNKNOWN are rejected.
 */
@Component
public class IndiaJobLocationValidator {

    private final JobLocationParser locationParser;
    private final CountryNormalizer countryNormalizer;

    public IndiaJobLocationValidator(JobLocationParser locationParser, CountryNormalizer countryNormalizer) {
        this.locationParser = locationParser;
        this.countryNormalizer = countryNormalizer;
    }

    public LocationClassification validate(String rawLocation, String title, String description) {
        JobLocationParser.ParsedLocation parsed = locationParser.parse(rawLocation, title, description);
        return parsed.classification();
    }

    public LocationClassification validate(AggregatedJob job) {
        if (job == null) {
            return LocationClassification.UNKNOWN;
        }
        return validate(job.getLocation(), job.getTitle(), job.getDescription());
    }

    public boolean isIndia(String rawLocation, String title, String description) {
        return validate(rawLocation, title, description) == LocationClassification.INDIA;
    }

    public boolean isIndia(AggregatedJob job) {
        return validate(job) == LocationClassification.INDIA;
    }

    public CountryNormalizer getCountryNormalizer() {
        return countryNormalizer;
    }
}
