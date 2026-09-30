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
            com.jobrecruitment.entity.RoleCategory roleCategory,
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

            // 2. Strict India-Only & Strict 0-Year Policy (Section 2, 3, 5, 12, 14, 16)
            // When querying fresher jobs (isFresher == true), ALWAYS enforce:
            // - Country == INDIA
            // - LocationClassification == INDIA
            // - isFresher == true
            // - eligibilityStatus == ELIGIBLE_ZERO_YEAR
            // - minimumExperienceYears == 0
            // - isActive == true
            // - roleCategory != OTHER (Strict IT/Software only, rejecting HR, Sales, Marketing, etc.)
            if (Boolean.TRUE.equals(isFresher)) {
                predicates.add(cb.equal(cb.upper(root.get("country")), "INDIA"));
                predicates.add(cb.equal(root.get("locationClassification"), LocationClassification.INDIA));
                predicates.add(cb.equal(root.get("isFresher"), true));
                predicates.add(cb.equal(root.get("eligibilityStatus"), EligibilityStatus.ELIGIBLE_ZERO_YEAR));
                predicates.add(cb.equal(root.get("minimumExperienceYears"), 0));
                predicates.add(cb.equal(root.get("isActive"), true));
                predicates.add(cb.notEqual(root.get("roleCategory"), com.jobrecruitment.entity.RoleCategory.OTHER));
            } else if (Boolean.FALSE.equals(isFresher)) {
                predicates.add(cb.equal(root.get("isFresher"), false));
            }

            // 3. Role category filter (e.g. JAVA_FULL_STACK, JAVA_BACKEND, etc.)
            if (roleCategory != null) {
                predicates.add(cb.equal(root.get("roleCategory"), roleCategory));
            }

            // 4. Keyword filter across title, description, skills, companyName
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate skillsMatch = cb.like(cb.lower(root.get("skills")), pattern);
                Predicate companyMatch = cb.like(cb.lower(root.get("companyName")), pattern);
                predicates.add(cb.or(titleMatch, descMatch, skillsMatch, companyMatch));
            }

            // 5. Location filter (City, State, Location text)
            if (location != null && !location.trim().isEmpty()) {
                String loc = location.trim().toLowerCase();
                String pattern = "%" + loc + "%";
                Predicate locMatch = cb.like(cb.lower(root.get("location")), pattern);
                Predicate cityMatch = cb.like(cb.lower(root.get("city")), pattern);
                Predicate stateMatch = cb.like(cb.lower(root.get("state")), pattern);
                predicates.add(cb.or(locMatch, cityMatch, stateMatch));
            }

            // 6. Role keyword filter
            if (role != null && !role.trim().isEmpty()) {
                String pattern = "%" + role.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), pattern));
            }

            // 7. Company name filter
            if (company != null && !company.trim().isEmpty()) {
                String pattern = "%" + company.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("companyName")), pattern));
            }

            // 8. Remote filter
            if (remote != null) {
                predicates.add(cb.equal(root.get("remote"), remote));
            }

            // 9. Experience level filter
            if (experienceLevel != null) {
                predicates.add(cb.equal(root.get("experienceLevel"), experienceLevel));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    // Overload for backward compatibility
    public static Specification<AggregatedJob> withFilters(
            String keyword,
            String location,
            String role,
            String company,
            Boolean remote,
            ExperienceLevel experienceLevel,
            Boolean isFresher,
            Boolean isActive) {
        return withFilters(keyword, location, role, company, remote, experienceLevel, null, isFresher, isActive);
    }
}
