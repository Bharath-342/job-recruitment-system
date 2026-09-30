package com.jobrecruitment.repository;

import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.EligibilityStatus;
import com.jobrecruitment.entity.ExperienceLevel;
import com.jobrecruitment.entity.LocationClassification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class AggregatedJobSpecifications {

    public static Specification<AggregatedJob> withFilters(
            String keyword,
            String location,
            String role,
            String company,
            Boolean remote,
            ExperienceLevel experienceLevel,
            Boolean isFresher,
            Boolean isActive) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Active status filter (default: true)
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            } else {
                predicates.add(cb.equal(root.get("isActive"), true));
            }

            // 2. Strict India-Only & Strict 0-Year Policy (Section 2, 3, 5, 12)
            // When querying fresher jobs (isFresher == true), ALWAYS enforce:
            // - Country == India OR LocationClassification == INDIA
            // - isFresher == true
            // - eligibilityStatus == ELIGIBLE_ZERO_YEAR
            // - minimumExperienceYears == 0
            if (Boolean.TRUE.equals(isFresher)) {
                // Country India check
                Predicate countryIsIndia = cb.equal(cb.lower(root.get("country")), "india");
                Predicate locClassIsIndia = cb.equal(root.get("locationClassification"), LocationClassification.INDIA);
                predicates.add(cb.or(countryIsIndia, locClassIsIndia));

                // Strict 0-Year experience check
                predicates.add(cb.equal(root.get("isFresher"), true));
                predicates.add(cb.equal(root.get("eligibilityStatus"), EligibilityStatus.ELIGIBLE_ZERO_YEAR));
                predicates.add(cb.equal(root.get("minimumExperienceYears"), 0));
            } else if (Boolean.FALSE.equals(isFresher)) {
                predicates.add(cb.equal(root.get("isFresher"), false));
            }

            // 3. Keyword filter across title, description, skills, companyName
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate skillsMatch = cb.like(cb.lower(root.get("skills")), pattern);
                Predicate companyMatch = cb.like(cb.lower(root.get("companyName")), pattern);
                predicates.add(cb.or(titleMatch, descMatch, skillsMatch, companyMatch));
            }

            // 4. Location filter (City, State, Location text)
            if (location != null && !location.trim().isEmpty()) {
                String loc = location.trim().toLowerCase();
                String pattern = "%" + loc + "%";
                Predicate locMatch = cb.like(cb.lower(root.get("location")), pattern);
                Predicate cityMatch = cb.like(cb.lower(root.get("city")), pattern);
                Predicate stateMatch = cb.like(cb.lower(root.get("state")), pattern);
                predicates.add(cb.or(locMatch, cityMatch, stateMatch));
            }

            // 5. Role keyword filter
            if (role != null && !role.trim().isEmpty()) {
                String pattern = "%" + role.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), pattern));
            }

            // 6. Company name filter
            if (company != null && !company.trim().isEmpty()) {
                String pattern = "%" + company.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("companyName")), pattern));
            }

            // 7. Remote filter
            if (remote != null) {
                predicates.add(cb.equal(root.get("remote"), remote));
            }

            // 8. Experience level filter
            if (experienceLevel != null) {
                predicates.add(cb.equal(root.get("experienceLevel"), experienceLevel));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
