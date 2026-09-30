package com.jobrecruitment.dto.response;

import java.time.LocalDateTime;

public class SyncSummaryDto {
    private int sourcesProcessed;
    private int sourcesSuccessful;
    private int sourcesFailed;
    private int totalJobsDiscovered;
    private int jobsInserted;
    private int jobsUpdated;
    private int jobsDeactivated;
    private int fresherJobsIdentified;
    private int duplicatesDetected;
    private LocalDateTime timestamp = LocalDateTime.now();

    public SyncSummaryDto() {}

    public int getSourcesProcessed() { return sourcesProcessed; }
    public void setSourcesProcessed(int sourcesProcessed) { this.sourcesProcessed = sourcesProcessed; }

    public int getSourcesSuccessful() { return sourcesSuccessful; }
    public void setSourcesSuccessful(int sourcesSuccessful) { this.sourcesSuccessful = sourcesSuccessful; }

    public int getSourcesFailed() { return sourcesFailed; }
    public void setSourcesFailed(int sourcesFailed) { this.sourcesFailed = sourcesFailed; }

    public int getTotalJobsDiscovered() { return totalJobsDiscovered; }
    public void setTotalJobsDiscovered(int totalJobsDiscovered) { this.totalJobsDiscovered = totalJobsDiscovered; }

    public int getJobsInserted() { return jobsInserted; }
    public void setJobsInserted(int jobsInserted) { this.jobsInserted = jobsInserted; }

    public int getJobsUpdated() { return jobsUpdated; }
    public void setJobsUpdated(int jobsUpdated) { this.jobsUpdated = jobsUpdated; }

    public int getJobsDeactivated() { return jobsDeactivated; }
    public void setJobsDeactivated(int jobsDeactivated) { this.jobsDeactivated = jobsDeactivated; }

    public int getFresherJobsIdentified() { return fresherJobsIdentified; }
    public void setFresherJobsIdentified(int fresherJobsIdentified) { this.fresherJobsIdentified = fresherJobsIdentified; }

    public int getDuplicatesDetected() { return duplicatesDetected; }
    public void setDuplicatesDetected(int duplicatesDetected) { this.duplicatesDetected = duplicatesDetected; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
