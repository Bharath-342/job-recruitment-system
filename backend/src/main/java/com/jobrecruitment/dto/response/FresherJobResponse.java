package com.jobrecruitment.dto.response;

import com.jobrecruitment.entity.AggregatedJob;
import com.jobrecruitment.entity.EligibilityStatus;
import com.jobrecruitment.entity.ExperienceLevel;
import com.jobrecruitment.entity.LocationClassification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FresherJobResponse {
    private Long id;
    private String externalJobId;
    private Long companyId;
    private String companyName;
    private String title;
    private String description;
    private String location;
    private String country;
    private String state;
    private String city;
    private LocationClassification locationClassification;
    private String employmentType;
    private ExperienceLevel experienceLevel;
    private String department;
    private LocalDateTime postedAt;
    private String applicationUrl;
    private String sourceUrl;
    private String sourceProvider;
    private boolean remote;
    private BigDecimal salaryMin;
    private BigDecimal salaryMax;
    private String currency;
    private String skills;
    private String experienceText;
    private Integer minimumExperienceYears;
    private Integer maximumExperienceYears;
    private EligibilityStatus eligibilityStatus;
    private boolean isFresher;
    private Integer fresherConfidence;
    private boolean isActive;
    private LocalDateTime lastVerifiedAt;
    private LocalDateTime createdAt;

    public FresherJobResponse() {}

    public static FresherJobResponse fromEntity(AggregatedJob job) {
        FresherJobResponse res = new FresherJobResponse();
        res.id = job.getId();
        res.externalJobId = job.getExternalJobId();
        res.companyId = job.getCompanyId();
        res.companyName = job.getCompanyName();
        res.title = job.getTitle();
        res.description = job.getDescription();
        res.location = job.getLocation();
        res.country = job.getCountry();
        res.state = job.getState();
        res.city = job.getCity();
        res.locationClassification = job.getLocationClassification();
        res.employmentType = job.getEmploymentType();
        res.experienceLevel = job.getExperienceLevel();
        res.department = job.getDepartment();
        res.postedAt = job.getPostedAt();
        res.applicationUrl = job.getApplicationUrl();
        res.sourceUrl = job.getSourceUrl();
        res.sourceProvider = job.getSourceProvider();
        res.remote = job.isRemote();
        res.salaryMin = job.getSalaryMin();
        res.salaryMax = job.getSalaryMax();
        res.currency = job.getCurrency();
        res.skills = job.getSkills();
        res.experienceText = job.getExperienceText();
        res.minimumExperienceYears = job.getMinimumExperienceYears();
        res.maximumExperienceYears = job.getMaximumExperienceYears();
        res.eligibilityStatus = job.getEligibilityStatus();
        res.isFresher = job.isFresher();
        res.fresherConfidence = job.getFresherConfidence();
        res.isActive = job.isActive();
        res.lastVerifiedAt = job.getLastVerifiedAt();
        res.createdAt = job.getCreatedAt();
        return res;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getExternalJobId() { return externalJobId; }
    public void setExternalJobId(String externalJobId) { this.externalJobId = externalJobId; }

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

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

    public Integer getMinimumExperienceYears() { return minimumExperienceYears; }
    public void setMinimumExperienceYears(Integer minimumExperienceYears) { this.minimumExperienceYears = minimumExperienceYears; }

    public Integer getMaximumExperienceYears() { return maximumExperienceYears; }
    public void setMaximumExperienceYears(Integer maximumExperienceYears) { this.maximumExperienceYears = maximumExperienceYears; }

    public EligibilityStatus getEligibilityStatus() { return eligibilityStatus; }
    public void setEligibilityStatus(EligibilityStatus eligibilityStatus) { this.eligibilityStatus = eligibilityStatus; }

    public boolean isFresher() { return isFresher; }
    public void setFresher(boolean fresher) { isFresher = fresher; }

    public Integer getFresherConfidence() { return fresherConfidence; }
    public void setFresherConfidence(Integer fresherConfidence) { this.fresherConfidence = fresherConfidence; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getLastVerifiedAt() { return lastVerifiedAt; }
    public void setLastVerifiedAt(LocalDateTime lastVerifiedAt) { this.lastVerifiedAt = lastVerifiedAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
