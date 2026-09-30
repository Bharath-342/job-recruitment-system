package com.jobrecruitment.repository;

import com.jobrecruitment.entity.Application;
import com.jobrecruitment.entity.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {

    boolean existsByCandidateIdAndJobId(Long candidateId, Long jobId);

    Page<Application> findByCandidateId(Long candidateId, Pageable pageable);

    @Query("SELECT a FROM Application a JOIN FETCH a.job j JOIN FETCH a.candidate c WHERE a.job.recruiter.id = :recruiterId")
    Page<Application> findByJobRecruiterId(@Param("recruiterId") Long recruiterId, Pageable pageable);

    Page<Application> findByJobId(Long jobId, Pageable pageable);

    @Query("SELECT a FROM Application a JOIN FETCH a.job WHERE a.id = :id")
    Optional<Application> findByIdWithJob(@Param("id") Long id);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId AND a.status = :status")
    long countByCandidateIdAndStatus(@Param("candidateId") Long candidateId, @Param("status") ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter.id = :recruiterId AND a.status = :status")
    long countByJobRecruiterIdAndStatus(@Param("recruiterId") Long recruiterId, @Param("status") ApplicationStatus status);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.recruiter.id = :recruiterId")
    long countByJobRecruiterId(@Param("recruiterId") Long recruiterId);

    @Query("SELECT COUNT(a) FROM Application a WHERE a.candidate.id = :candidateId")
    long countByCandidateId(@Param("candidateId") Long candidateId);

    long countByStatus(ApplicationStatus status);
}
