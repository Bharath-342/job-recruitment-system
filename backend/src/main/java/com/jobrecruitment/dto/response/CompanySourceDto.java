package com.jobrecruitment.dto.response;

import com.jobrecruitment.entity.CompanySource;
import java.time.LocalDateTime;

public class CompanySourceDto {
    private Long id;
    private String companyName;
    private String careersUrl;
    private String sourceType;
    private String provider;
    private String providerIdentifier;
    private String country;
    private boolean enabled;
    private LocalDateTime lastSuccessfulSync;
    private LocalDateTime lastAttemptedSync;
    private String status;
    private String errorMessage;
    private Integer totalJobsFound;
    private Integer fresherJobsFound;

    public CompanySourceDto() {}

    public static CompanySourceDto fromEntity(CompanySource source) {
        CompanySourceDto dto = new CompanySourceDto();
        dto.id = source.getId();
        dto.companyName = source.getCompanyName();
        dto.careersUrl = source.getCareersUrl();
        dto.sourceType = source.getSourceType();
        dto.provider = source.getProvider();
        dto.providerIdentifier = source.getProviderIdentifier();
        dto.country = source.getCountry();
        dto.enabled = source.isEnabled();
        dto.lastSuccessfulSync = source.getLastSuccessfulSync();
        dto.lastAttemptedSync = source.getLastAttemptedSync();
        dto.status = source.getStatus();
        dto.errorMessage = source.getErrorMessage();
        dto.totalJobsFound = source.getTotalJobsFound();
        dto.fresherJobsFound = source.getFresherJobsFound();
        return dto;
    }

    public Long getId() { return id; }
    public String getCompanyName() { return companyName; }
    public String getCareersUrl() { return careersUrl; }
    public String getSourceType() { return sourceType; }
    public String getProvider() { return provider; }
    public String getProviderIdentifier() { return providerIdentifier; }
    public String getCountry() { return country; }
    public boolean isEnabled() { return enabled; }
    public LocalDateTime getLastSuccessfulSync() { return lastSuccessfulSync; }
    public LocalDateTime getLastAttemptedSync() { return lastAttemptedSync; }
    public String getStatus() { return status; }
    public String getErrorMessage() { return errorMessage; }
    public Integer getTotalJobsFound() { return totalJobsFound; }
    public Integer getFresherJobsFound() { return fresherJobsFound; }
}
