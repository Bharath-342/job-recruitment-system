package com.jobrecruitment.dto.response;

import java.time.LocalDateTime;

public class JobStatisticsResponse {
    private long totalActiveFresherJobs;
    private long uniqueCompaniesHiring;
    private long locations;
    private long remoteJobs;
    private LocalDateTime lastSyncedAt;

    public JobStatisticsResponse() {}

    public JobStatisticsResponse(long totalActiveFresherJobs, long uniqueCompaniesHiring, long locations, long remoteJobs, LocalDateTime lastSyncedAt) {
        this.totalActiveFresherJobs = totalActiveFresherJobs;
        this.uniqueCompaniesHiring = uniqueCompaniesHiring;
        this.locations = locations;
        this.remoteJobs = remoteJobs;
        this.lastSyncedAt = lastSyncedAt;
    }

    public long getTotalActiveFresherJobs() { return totalActiveFresherJobs; }
    public void setTotalActiveFresherJobs(long totalActiveFresherJobs) { this.totalActiveFresherJobs = totalActiveFresherJobs; }

    public long getUniqueCompaniesHiring() { return uniqueCompaniesHiring; }
    public void setUniqueCompaniesHiring(long uniqueCompaniesHiring) { this.uniqueCompaniesHiring = uniqueCompaniesHiring; }

    public long getLocations() { return locations; }
    public void setLocations(long locations) { this.locations = locations; }

    public long getRemoteJobs() { return remoteJobs; }
    public void setRemoteJobs(long remoteJobs) { this.remoteJobs = remoteJobs; }

    public LocalDateTime getLastSyncedAt() { return lastSyncedAt; }
    public void setLastSyncedAt(LocalDateTime lastSyncedAt) { this.lastSyncedAt = lastSyncedAt; }
}
