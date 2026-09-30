package com.jobrecruitment.repository;

import com.jobrecruitment.entity.JobDiscoveryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobDiscoveryRecordRepository extends JpaRepository<JobDiscoveryRecord, Long> {
    List<JobDiscoveryRecord> findByRejectionReason(String rejectionReason);
    Page<JobDiscoveryRecord> findBySourceProvider(String sourceProvider, Pageable pageable);
    long countByRejectionReason(String rejectionReason);
}
