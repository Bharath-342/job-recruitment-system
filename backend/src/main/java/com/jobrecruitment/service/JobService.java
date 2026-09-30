package com.jobrecruitment.service;

import com.jobrecruitment.dto.request.JobRequest;
import com.jobrecruitment.dto.response.JobResponse;
import com.jobrecruitment.entity.EmploymentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface JobService {
    JobResponse createJob(JobRequest request, String recruiterEmail);
    JobResponse updateJob(Long jobId, JobRequest request, String recruiterEmail);
    JobResponse getJobById(Long jobId);
    Page<JobResponse> searchJobs(String keyword, String location, String employmentType,
                                 Integer experience, BigDecimal salaryMin, Pageable pageable);
    Page<JobResponse> getRecruiterJobs(String recruiterEmail, String status, Pageable pageable);
    void closeJob(Long jobId, String recruiterEmail);
    void deleteJob(Long jobId, String userEmail, boolean isAdmin);
}
