package com.jobrecruitment.repository;

import com.jobrecruitment.entity.EmploymentType;
import com.jobrecruitment.entity.Job;
import com.jobrecruitment.entity.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {

    @Query("SELECT j FROM Job j WHERE j.status = 'OPEN' " +
           "AND (:keyword IS NULL OR LOWER(j.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.description) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(j.companyName) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:location IS NULL OR LOWER(j.location) LIKE LOWER(CONCAT('%', :location, '%'))) " +
           "AND (:employmentType IS NULL OR j.employmentType = :employmentType) " +
           "AND (:experienceMax IS NULL OR j.experienceMin <= :experienceMax) " +
           "AND (:salaryMin IS NULL OR j.salaryMax >= :salaryMin)")
    Page<Job> searchJobs(
            @Param("keyword") String keyword,
            @Param("location") String location,
            @Param("employmentType") EmploymentType employmentType,
            @Param("experienceMax") Integer experienceMax,
            @Param("salaryMin") java.math.BigDecimal salaryMin,
            Pageable pageable);

    Page<Job> findByRecruiterIdAndStatus(Long recruiterId, JobStatus status, Pageable pageable);

    Page<Job> findByRecruiterId(Long recruiterId, Pageable pageable);

    @Query("SELECT COUNT(j) FROM Job j WHERE j.recruiter.id = :recruiterId AND j.status = :status")
    long countByRecruiterIdAndStatus(@Param("recruiterId") Long recruiterId, @Param("status") JobStatus status);

    long countByStatus(JobStatus status);
}
