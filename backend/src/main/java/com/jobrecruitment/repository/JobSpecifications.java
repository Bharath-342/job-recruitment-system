package com.jobrecruitment.repository;

import com.jobrecruitment.entity.EmploymentType;
import com.jobrecruitment.entity.Job;
import com.jobrecruitment.entity.JobStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class JobSpecifications {

    public static Specification<Job> filterJobs(
            String keyword,
            String location,
            EmploymentType employmentType,
            Integer experienceMax,
            BigDecimal salaryMin) {

        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Only OPEN jobs
            predicates.add(cb.equal(root.get("status"), JobStatus.OPEN));

            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.toLowerCase() + "%";
                Predicate title = cb.like(cb.lower(root.get("title")), pattern);
                Predicate desc = cb.like(cb.lower(root.get("description")), pattern);
                Predicate company = cb.like(cb.lower(root.get("companyName")), pattern);
                predicates.add(cb.or(title, desc, company));
            }

            if (location != null && !location.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("location")), "%" + location.toLowerCase() + "%"));
            }

            if (employmentType != null) {
                predicates.add(cb.equal(root.get("employmentType"), employmentType));
            }

            if (experienceMax != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experienceMin"), experienceMax));
            }

            if (salaryMin != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMax"), salaryMin));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
