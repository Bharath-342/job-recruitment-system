package com.jobrecruitment.repository;

import com.jobrecruitment.entity.AggregatedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AggregatedJobRepository extends JpaRepository<AggregatedJob, Long>, JpaSpecificationExecutor<AggregatedJob> {

    List<AggregatedJob> findBySourceProviderAndExternalJobId(String sourceProvider, String externalJobId);

    List<AggregatedJob> findByCompanyNameIgnoreCaseAndTitleIgnoreCaseAndLocationIgnoreCase(
            String companyName, String title, String location);

    List<AggregatedJob> findByCompanyId(Long companyId);

    List<AggregatedJob> findBySourceProvider(String sourceProvider);

    // Strict India + Strict 0-Year Fresher Count Queries (Sections 3, 12, 14)
    @Query("SELECT COUNT(j) FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND UPPER(j.country) = 'INDIA' AND j.locationClassification = 'INDIA' AND j.eligibilityStatus = 'ELIGIBLE_ZERO_YEAR' AND j.minimumExperienceYears = 0")
    long countStrictFresherJobs();

    @Query("SELECT COUNT(DISTINCT j.companyName) FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND UPPER(j.country) = 'INDIA' AND j.locationClassification = 'INDIA' AND j.eligibilityStatus = 'ELIGIBLE_ZERO_YEAR' AND j.minimumExperienceYears = 0")
    long countDistinctCompaniesHiringFreshers();

    @Query("SELECT COUNT(DISTINCT j.location) FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND UPPER(j.country) = 'INDIA' AND j.locationClassification = 'INDIA' AND j.eligibilityStatus = 'ELIGIBLE_ZERO_YEAR' AND j.minimumExperienceYears = 0 AND j.location IS NOT NULL")
    long countDistinctLocationsHiringFreshers();

    @Query("SELECT COUNT(j) FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND UPPER(j.country) = 'INDIA' AND j.locationClassification = 'INDIA' AND j.eligibilityStatus = 'ELIGIBLE_ZERO_YEAR' AND j.minimumExperienceYears = 0 AND j.remote = true")
    long countStrictRemoteFresherJobs();

    @Query("SELECT DISTINCT j.companyName FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND UPPER(j.country) = 'INDIA' AND j.locationClassification = 'INDIA' AND j.eligibilityStatus = 'ELIGIBLE_ZERO_YEAR' AND j.minimumExperienceYears = 0 ORDER BY j.companyName ASC")
    List<String> findDistinctCompaniesHiringFreshers();

    @Query("SELECT DISTINCT j.location FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND UPPER(j.country) = 'INDIA' AND j.locationClassification = 'INDIA' AND j.eligibilityStatus = 'ELIGIBLE_ZERO_YEAR' AND j.minimumExperienceYears = 0 AND j.location IS NOT NULL ORDER BY j.location ASC")
    List<String> findDistinctLocationsHiringFreshers();

    @Query("SELECT MAX(j.lastVerifiedAt) FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND UPPER(j.country) = 'INDIA' AND j.locationClassification = 'INDIA' AND j.eligibilityStatus = 'ELIGIBLE_ZERO_YEAR' AND j.minimumExperienceYears = 0")
    LocalDateTime findLatestVerificationTimestamp();

    @Query("SELECT new com.jobrecruitment.dto.response.CompanyDirectoryItemDto(j.companyName, COUNT(j.id), MAX(j.lastVerifiedAt)) " +
           "FROM AggregatedJob j " +
           "WHERE j.isActive = true AND j.isFresher = true AND UPPER(j.country) = 'INDIA' AND j.locationClassification = 'INDIA' AND j.eligibilityStatus = 'ELIGIBLE_ZERO_YEAR' AND j.minimumExperienceYears = 0 " +
           "GROUP BY j.companyName ORDER BY COUNT(j.id) DESC")
    Page<com.jobrecruitment.dto.response.CompanyDirectoryItemDto> findCompaniesHiringFreshers(Pageable pageable);

    // Audit queries for validation
    @Query("SELECT COUNT(j) FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND j.minimumExperienceYears > 0")
    long countActiveFresherWithExperienceGreaterThanZero();

    @Query("SELECT COUNT(j) FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND j.eligibilityStatus = 'UNKNOWN'")
    long countActiveFresherWithUnknownExperience();

    @Query("SELECT COUNT(j) FROM AggregatedJob j WHERE j.isActive = true AND j.isFresher = true AND (UPPER(j.country) != 'INDIA' OR j.locationClassification != 'INDIA')")
    long countActiveFresherOutsideIndia();
}
