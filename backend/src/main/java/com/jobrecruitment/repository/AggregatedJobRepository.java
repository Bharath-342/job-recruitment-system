package com.jobrecruitment.repository;

import com.jobrecruitment.entity.AggregatedJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AggregatedJobRepository extends JpaRepository<AggregatedJob, Long>, JpaSpecificationExecutor<AggregatedJob> {

    Optional<AggregatedJob> findBySourceProviderAndExternalJobId(String sourceProvider, String externalJobId);

    Optional<AggregatedJob> findByCompanyNameIgnoreCaseAndTitleIgnoreCaseAndLocationIgnoreCase(
            String companyName, String title, String location);

    List<AggregatedJob> findByCompanyId(Long companyId);

    List<AggregatedJob> findBySourceProvider(String sourceProvider);

    long countByIsActiveTrueAndIsFresherTrue();

    @Query("SELECT COUNT(DISTINCT j.companyName) FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true")
    long countDistinctCompaniesHiringFreshers();

    @Query("SELECT COUNT(DISTINCT j.location) FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND j.location IS NOT NULL")
    long countDistinctLocationsHiringFreshers();

    long countByIsActiveTrueAndIsFresherTrueAndRemoteTrue();

    @Query("SELECT DISTINCT j.companyName FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true ORDER BY j.companyName ASC")
    List<String> findDistinctCompaniesHiringFreshers();

    @Query("SELECT DISTINCT j.location FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND j.location IS NOT NULL ORDER BY j.location ASC")
    List<String> findDistinctLocationsHiringFreshers();

    @Query("SELECT MAX(j.lastVerifiedAt) FROM AggregatedJob j WHERE j.isActive = true")
    LocalDateTime findLatestVerificationTimestamp();
}
