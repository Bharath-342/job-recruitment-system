package com.jobrecruitment.dto.response;

import java.time.LocalDateTime;

public class CompanyDirectoryItemDto {
    private String companyName;
    private long activeFresherJobs;
    private LocalDateTime latestVerifiedAt;

    public CompanyDirectoryItemDto() {}

    public CompanyDirectoryItemDto(String companyName, long activeFresherJobs, LocalDateTime latestVerifiedAt) {
        this.companyName = companyName;
        this.activeFresherJobs = activeFresherJobs;
        this.latestVerifiedAt = latestVerifiedAt;
    }

    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }

    public long getActiveFresherJobs() { return activeFresherJobs; }
    public void setActiveFresherJobs(long activeFresherJobs) { this.activeFresherJobs = activeFresherJobs; }

    public LocalDateTime getLatestVerifiedAt() { return latestVerifiedAt; }
    public void setLatestVerifiedAt(LocalDateTime latestVerifiedAt) { this.latestVerifiedAt = latestVerifiedAt; }
}
