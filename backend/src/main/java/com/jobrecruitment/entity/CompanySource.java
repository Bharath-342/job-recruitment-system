package com.jobrecruitment.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "company_sources", indexes = {
    @Index(name = "idx_cs_provider", columnList = "provider"),
    @Index(name = "idx_cs_enabled", columnList = "enabled"),
    @Index(name = "idx_cs_company", columnList = "company_name")
})
public class CompanySource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(name = "careers_url", length = 500)
    private String careersUrl;

    @Column(name = "source_type", length = 50)
    private String sourceType = "ATS";

    @Column(nullable = false, length = 50)
    private String provider; // GREENHOUSE, LEVER, ASHBY, SMARTRECRUITERS

    @Column(name = "provider_identifier", nullable = false, length = 100)
    private String providerIdentifier; // e.g. "canonical", "stripe"

    @Column(length = 100)
    private String country = "India";

    @Column(nullable = false)
    private boolean enabled = true;

    private LocalDateTime lastSuccessfulSync;
    private LocalDateTime lastAttemptedSync;

    @Column(length = 50)
    private String status = "INITIALIZED"; // INITIALIZED, SYNCING, SUCCESS, ERROR

    @Column(name = "error_message", length = 1000)
    private String errorMessage;

    private Integer totalJobsFound = 0;
    private Integer fresherJobsFound = 0;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public CompanySource() {}

    public CompanySource(String companyName, String careersUrl, String provider, String providerIdentifier, String country) {
        this.companyName = companyName;
        this.careersUrl = careersUrl;
        this.provider = provider;
        this.providerIdentifier = providerIdentifier;
        this.country = country;
        this.enabled = true;
        this.status = "INITIALIZED";
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCareersUrl() { return careersUrl; }
    public void setCareersUrl(String careersUrl) { this.careersUrl = careersUrl; }

    public String getSourceType() { return sourceType; }
    public void setSourceType(String sourceType) { this.sourceType = sourceType; }

    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }

    public String getProviderIdentifier() { return providerIdentifier; }
    public void setProviderIdentifier(String providerIdentifier) { this.providerIdentifier = providerIdentifier; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public LocalDateTime getLastSuccessfulSync() { return lastSuccessfulSync; }
    public void setLastSuccessfulSync(LocalDateTime lastSuccessfulSync) { this.lastSuccessfulSync = lastSuccessfulSync; }

    public LocalDateTime getLastAttemptedSync() { return lastAttemptedSync; }
    public void setLastAttemptedSync(LocalDateTime lastAttemptedSync) { this.lastAttemptedSync = lastAttemptedSync; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public Integer getTotalJobsFound() { return totalJobsFound; }
    public void setTotalJobsFound(Integer totalJobsFound) { this.totalJobsFound = totalJobsFound; }

    public Integer getFresherJobsFound() { return fresherJobsFound; }
    public void setFresherJobsFound(Integer fresherJobsFound) { this.fresherJobsFound = fresherJobsFound; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
