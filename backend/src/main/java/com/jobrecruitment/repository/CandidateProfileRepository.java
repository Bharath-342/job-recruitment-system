package com.jobrecruitment.repository;

import com.jobrecruitment.entity.CandidateProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateProfileRepository extends JpaRepository<CandidateProfile, Long> {

    @Query("SELECT cp FROM CandidateProfile cp LEFT JOIN FETCH cp.skills WHERE cp.user.id = :userId")
    Optional<CandidateProfile> findByUserIdWithSkills(@Param("userId") Long userId);

    Optional<CandidateProfile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);
}
