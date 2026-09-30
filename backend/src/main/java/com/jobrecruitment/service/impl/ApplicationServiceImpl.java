package com.jobrecruitment.service.impl;

import com.jobrecruitment.dto.request.ApplicationRequest;
import com.jobrecruitment.dto.request.StatusUpdateRequest;
import com.jobrecruitment.dto.response.ApplicationResponse;
import com.jobrecruitment.entity.*;
import com.jobrecruitment.exception.BusinessRuleException;
import com.jobrecruitment.exception.DuplicateResourceException;
import com.jobrecruitment.exception.ResourceNotFoundException;
import com.jobrecruitment.mapper.EntityMapper;
import com.jobrecruitment.repository.ApplicationRepository;
import com.jobrecruitment.repository.JobRepository;
import com.jobrecruitment.repository.UserRepository;
import com.jobrecruitment.service.ApplicationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Set;

@Service
public class ApplicationServiceImpl implements ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    // Valid status transitions to enforce workflow
    private static final Map<ApplicationStatus, Set<ApplicationStatus>> VALID_TRANSITIONS = Map.of(
            ApplicationStatus.APPLIED, Set.of(ApplicationStatus.UNDER_REVIEW, ApplicationStatus.REJECTED),
            ApplicationStatus.UNDER_REVIEW, Set.of(ApplicationStatus.SHORTLISTED, ApplicationStatus.REJECTED),
            ApplicationStatus.SHORTLISTED, Set.of(ApplicationStatus.INTERVIEW, ApplicationStatus.REJECTED),
            ApplicationStatus.INTERVIEW, Set.of(ApplicationStatus.SELECTED, ApplicationStatus.REJECTED),
            ApplicationStatus.SELECTED, Set.of(),
            ApplicationStatus.REJECTED, Set.of()
    );

    public ApplicationServiceImpl(ApplicationRepository applicationRepository,
                                   JobRepository jobRepository, UserRepository userRepository) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public ApplicationResponse applyForJob(Long jobId, ApplicationRequest request, String candidateEmail) {
        User candidate = getUserByEmail(candidateEmail);
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (job.getStatus() != JobStatus.OPEN) {
            throw new BusinessRuleException("This job is no longer accepting applications");
        }

        if (applicationRepository.existsByCandidateIdAndJobId(candidate.getId(), jobId)) {
            throw new DuplicateResourceException("You have already applied for this job");
        }

        Application application = new Application();
        application.setCandidate(candidate);
        application.setJob(job);
        application.setStatus(ApplicationStatus.APPLIED);
        application.setCoverLetter(request.getCoverLetter());

        Application saved = applicationRepository.save(application);
        return EntityMapper.toApplicationResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getCandidateApplications(String candidateEmail, Pageable pageable) {
        User candidate = getUserByEmail(candidateEmail);
        return applicationRepository.findByCandidateId(candidate.getId(), pageable)
                .map(EntityMapper::toApplicationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getRecruiterApplications(String recruiterEmail, Pageable pageable) {
        User recruiter = getUserByEmail(recruiterEmail);
        return applicationRepository.findByJobRecruiterId(recruiter.getId(), pageable)
                .map(EntityMapper::toApplicationResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ApplicationResponse> getApplicationsByJob(Long jobId, String recruiterEmail, Pageable pageable) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new BusinessRuleException("You can only view applications for your own jobs");
        }

        return applicationRepository.findByJobId(jobId, pageable)
                .map(EntityMapper::toApplicationResponse);
    }

    @Override
    @Transactional
    public ApplicationResponse updateApplicationStatus(Long applicationId, StatusUpdateRequest request,
                                                        String recruiterEmail) {
        Application application = applicationRepository.findByIdWithJob(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!application.getJob().getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new BusinessRuleException("You can only update applications for your own jobs");
        }

        ApplicationStatus newStatus;
        try {
            newStatus = ApplicationStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid application status: " + request.getStatus());
        }

        ApplicationStatus currentStatus = application.getStatus();
        Set<ApplicationStatus> validNext = VALID_TRANSITIONS.getOrDefault(currentStatus, Set.of());

        if (!validNext.contains(newStatus)) {
            throw new BusinessRuleException(
                    String.format("Cannot transition from %s to %s", currentStatus, newStatus));
        }

        application.setStatus(newStatus);
        if (request.getNotes() != null) {
            application.setRecruiterNotes(request.getNotes());
        }

        Application updated = applicationRepository.save(application);
        return EntityMapper.toApplicationResponse(updated);
    }

    @Override
    @Transactional
    public void withdrawApplication(Long applicationId, String candidateEmail) {
        Application application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + applicationId));

        if (!application.getCandidate().getEmail().equals(candidateEmail)) {
            throw new BusinessRuleException("You can only withdraw your own applications");
        }

        if (application.getStatus() != ApplicationStatus.APPLIED &&
            application.getStatus() != ApplicationStatus.UNDER_REVIEW) {
            throw new BusinessRuleException("Cannot withdraw application at this stage");
        }

        applicationRepository.delete(application);
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
