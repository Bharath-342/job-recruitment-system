package com.jobrecruitment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "job_discovery_records", indexes = {
    @Index(name = "idx_disc_source", columnList = "sourceProvider"),
    @Index(name = "idx_disc_reason", columnList = "rejectionReason"),
    @Index(name = "idx_disc_date", columnList = "discoveredAt")
})
public class JobDiscoveryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String sourceProvider;

    @Column(length = 150)
    private String externalJobId;

    @Column(length = 200)
    private String companyName;

    @Column(nullable = false, length = 300)
    private String title;

    @Column(length = 300)
    private String location;

    @Column(length = 500)
    private String experienceText;

    @Column(nullable = false, length = 50)
    private String rejectionReason; // FOREIGN_COUNTRY, UNKNOWN_COUNTRY, EXPERIENCE_REQUIRED, UNKNOWN_EXPERIENCE, DUPLICATE, INVALID_JOB, EXPIRED

    @Column(nullable = false)
    private LocalDateTime discoveredAt = LocalDateTime.now();

    public JobDiscoveryRecord() {}

    public JobDiscoveryRecord(String sourceProvider, String externalJobId, String companyName,
                              String title, String location, String experienceText, String rejectionReason) {
        this.sourceProvider = sourceProvider;
        this.externalJobId = externalJobId;
        this.companyName = companyName;
        this.title = title;
        this.location = location;
        this.experienceText = experienceText;
        this.rejectionReason = rejectionReason;
        this.discoveredAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSourceProvider() { return sourceProvider; }
    public void setSourceProvider(String sourceProvider) { this.sourceProvider = sourceProvider; }

    public String getExternalJobId() { return externalJobId; }
    public void setExternalJobId(String externalJobId) { this.externalJobId = externalJobId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getExperienceText() { return experienceText; }
    public void setExperienceText(String experienceText) { this.experienceText = experienceText; }

    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }

    public LocalDateTime getDiscoveredAt() { return discoveredAt; }
    public void setDiscoveredAt(LocalDateTime discoveredAt) { this.discoveredAt = discoveredAt; }
}
