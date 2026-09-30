package com.jobrecruitment.service;

import com.jobrecruitment.dto.request.ApplicationRequest;
import com.jobrecruitment.dto.request.StatusUpdateRequest;
import com.jobrecruitment.dto.response.ApplicationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ApplicationService {
    ApplicationResponse applyForJob(Long jobId, ApplicationRequest request, String candidateEmail);
    Page<ApplicationResponse> getCandidateApplications(String candidateEmail, Pageable pageable);
    Page<ApplicationResponse> getRecruiterApplications(String recruiterEmail, Pageable pageable);
    Page<ApplicationResponse> getApplicationsByJob(Long jobId, String recruiterEmail, Pageable pageable);
    ApplicationResponse updateApplicationStatus(Long applicationId, StatusUpdateRequest request, String recruiterEmail);
    void withdrawApplication(Long applicationId, String candidateEmail);
}
