package com.jobrecruitment.repository;

import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.ExperienceLevel;
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

            // Active status filter (default: true)
            if (isActive != null) {
                predicates.add(cb.equal(root.get("isActive"), isActive));
            } else {
                predicates.add(cb.equal(root.get("isActive"), true));
            }

            // Fresher filter (default: true for fresher searches)
            if (isFresher != null) {
                predicates.add(cb.equal(root.get("isFresher"), isFresher));
            }

            // Keyword filter across title, description, skills, companyName
            if (keyword != null && !keyword.trim().isEmpty()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                Predicate skillsMatch = cb.like(cb.lower(root.get("skills")), pattern);
                Predicate companyMatch = cb.like(cb.lower(root.get("companyName")), pattern);
                predicates.add(cb.or(titleMatch, descMatch, skillsMatch, companyMatch));
            }

            // Location filter
            if (location != null && !location.trim().isEmpty()) {
                String loc = location.trim().toLowerCase();
                String pattern = "%" + loc + "%";
                Predicate locMatch = cb.like(cb.lower(root.get("location")), pattern);
                Predicate countryMatch = cb.like(cb.lower(root.get("country")), pattern);
                predicates.add(cb.or(locMatch, countryMatch));
            }

            // Role keyword filter
            if (role != null && !role.trim().isEmpty()) {
                String pattern = "%" + role.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("title")), pattern));
            }

            // Company name filter
            if (company != null && !company.trim().isEmpty()) {
                String pattern = "%" + company.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("companyName")), pattern));
            }

            // Remote filter
            if (remote != null) {
                predicates.add(cb.equal(root.get("remote"), remote));
            }

            // Experience level filter
            if (experienceLevel != null) {
                predicates.add(cb.equal(root.get("experienceLevel"), experienceLevel));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
