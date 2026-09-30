package com.jobrecruitment.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "aggregated_jobs", indexes = {
    @Index(name = "idx_agg_external_job", columnList = "external_job_id, source_provider"),
    @Index(name = "idx_agg_company_name", columnList = "company_name"),
    @Index(name = "idx_agg_title", columnList = "title"),
    @Index(name = "idx_agg_location", columnList = "location"),
    @Index(name = "idx_agg_country", columnList = "country"),
    @Index(name = "idx_agg_loc_class", columnList = "location_classification"),
    @Index(name = "idx_agg_is_active", columnList = "is_active"),
    @Index(name = "idx_agg_is_fresher", columnList = "is_fresher"),
    @Index(name = "idx_agg_posted_at", columnList = "posted_at"),
    @Index(name = "idx_agg_source_provider", columnList = "source_provider"),
    @Index(name = "idx_agg_remote", columnList = "remote"),
    @Index(name = "idx_agg_eligibility", columnList = "eligibility_status"),
    @Index(name = "idx_agg_min_exp", columnList = "minimum_experience_years")
})
public class AggregatedJob {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_job_id", nullable = false, length = 150)
    private String externalJobId;

    @Column(name = "company_id")
    private Long companyId;

    @Column(name = "company_name", nullable = false, length = 150)
    private String companyName;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 200)
    private String location;

    @Column(length = 100)
    private String country;

    @Column(name = "country_code", length = 10)
    private String countryCode;

    @Column(name = "country_name", length = 100)
    private String countryName;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String city;

    @Enumerated(EnumType.STRING)
    @Column(name = "location_classification", length = 30)
    private LocationClassification locationClassification = LocationClassification.UNKNOWN;

    @Column(name = "employment_type", length = 50)
    private String employmentType = "FULL_TIME";

    @Enumerated(EnumType.STRING)
    @Column(name = "experience_level", length = 30)
    private ExperienceLevel experienceLevel = ExperienceLevel.FRESHER;

    @Column(length = 150)
    private String department;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @Column(name = "application_url", length = 1000)
    private String applicationUrl;

    @Column(name = "source_url", length = 1000)
    private String sourceUrl;

    @Column(name = "source_provider", length = 50)
    private String sourceProvider;

    @Column(name = "source_job_id", length = 150)
    private String sourceJobId;

    @Column(name = "remote", nullable = false)
    private boolean remote = false;

    @Column(name = "salary_min", precision = 12, scale = 2)
    private BigDecimal salaryMin;

    @Column(name = "salary_max", precision = 12, scale = 2)
    private BigDecimal salaryMax;

    @Column(length = 10)
    private String currency = "INR";

    @Column(length = 500)
    private String skills;

    @Column(name = "experience_text", length = 500)
    private String experienceText;

    @Column(name = "minimum_experience_years")
    private Integer minimumExperienceYears = 0;

    @Column(name = "maximum_experience_years")
    private Integer maximumExperienceYears;

    @Enumerated(EnumType.STRING)
    @Column(name = "eligibility_status", length = 30)
    private EligibilityStatus eligibilityStatus = EligibilityStatus.ELIGIBLE_ZERO_YEAR;

    @Column(name = "is_fresher", nullable = false)
    private boolean isFresher = true;

    @Column(name = "fresher_confidence")
    private Integer fresherConfidence = 0;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @Column(name = "last_verified_at")
    private LocalDateTime lastVerifiedAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.lastVerifiedAt == null) {
            this.lastVerifiedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public AggregatedJob() {}

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExternalJobId() { return externalJobId; }
    public void setExternalJobId(String externalJobId) { this.externalJobId = externalJobId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getCompany() { return companyName; }
    public void setCompany(String company) { this.companyName = company; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCountryCode() { return countryCode; }
    public void setCountryCode(String countryCode) { this.countryCode = countryCode; }

    public String getCountryName() { return countryName; }
    public void setCountryName(String countryName) { this.countryName = countryName; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public LocationClassification getLocationClassification() { return locationClassification; }
    public void setLocationClassification(LocationClassification locationClassification) { this.locationClassification = locationClassification; }

    public String getEmploymentType() { return employmentType; }
    public void setEmploymentType(String employmentType) { this.employmentType = employmentType; }

    public ExperienceLevel getExperienceLevel() { return experienceLevel; }
    public void setExperienceLevel(ExperienceLevel experienceLevel) { this.experienceLevel = experienceLevel; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public LocalDateTime getPostedAt() { return postedAt; }
    public void setPostedAt(LocalDateTime postedAt) { this.postedAt = postedAt; }

    public String getApplicationUrl() { return applicationUrl; }
    public void setApplicationUrl(String applicationUrl) { this.applicationUrl = applicationUrl; }

    public String getSourceUrl() { return sourceUrl; }
    public void setSourceUrl(String sourceUrl) { this.sourceUrl = sourceUrl; }

    public String getSourceProvider() { return sourceProvider; }
    public void setSourceProvider(String sourceProvider) { this.sourceProvider = sourceProvider; }

    public String getSourceJobId() { return sourceJobId; }
    public void setSourceJobId(String sourceJobId) { this.sourceJobId = sourceJobId; }

    public boolean isRemote() { return remote; }
    public void setRemote(boolean remote) { this.remote = remote; }

    public BigDecimal getSalaryMin() { return salaryMin; }
    public void setSalaryMin(BigDecimal salaryMin) { this.salaryMin = salaryMin; }

    public BigDecimal getSalaryMax() { return salaryMax; }
    public void setSalaryMax(BigDecimal salaryMax) { this.salaryMax = salaryMax; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getExperienceText() { return experienceText; }
    public void setExperienceText(String experienceText) { this.experienceText = experienceText; }

    public boolean isFresher() { return isFresher; }
    public void setFresher(boolean fresher) { isFresher = fresher; }

    public boolean isFresherEligible() { return isFresher; }
    public void setFresherEligible(boolean fresherEligible) { this.isFresher = fresherEligible; }

    public Integer getFresherConfidence() { return fresherConfidence; }
    public void setFresherConfidence(Integer fresherConfidence) { this.fresherConfidence = fresherConfidence; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(LocalDateTime lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }

    public Integer getMinimumExperienceYears() { return minimumExperienceYears; }
    public void setMinimumExperienceYears(Integer minimumExperienceYears) { this.minimumExperienceYears = minimumExperienceYears; }

    public Integer getMaximumExperienceYears() { return maximumExperienceYears; }
    public void setMaximumExperienceYears(Integer maximumExperienceYears) { this.maximumExperienceYears = maximumExperienceYears; }

    public EligibilityStatus getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(EligibilityStatus eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }

    public EligibilityStatus getExperienceClassification() { return eligibilityStatus; }
    public void setExperienceClassification(EligibilityStatus status) { this.eligibilityStatus = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
