package com.jobrecruitment.service.impl;

import com.jobrecruitment.dto.request.JobRequest;
import com.jobrecruitment.dto.response.JobResponse;
import com.jobrecruitment.entity.*;
import com.jobrecruitment.exception.BusinessRuleException;
import com.jobrecruitment.exception.ResourceNotFoundException;
import com.jobrecruitment.mapper.EntityMapper;
import com.jobrecruitment.repository.*;
import com.jobrecruitment.service.JobService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Service
public class JobServiceImpl implements JobService {

    private final JobRepository jobRepository;
    private final UserRepository userRepository;
    private final SkillRepository skillRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final ApplicationRepository applicationRepository;

    public JobServiceImpl(JobRepository jobRepository, UserRepository userRepository,
                          SkillRepository skillRepository, RecruiterProfileRepository recruiterProfileRepository,
                          ApplicationRepository applicationRepository) {
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.skillRepository = skillRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    @Transactional
    public JobResponse createJob(JobRequest request, String recruiterEmail) {
        User recruiter = getUserByEmail(recruiterEmail);
        RecruiterProfile profile = recruiterProfileRepository.findByUserId(recruiter.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

        Job job = new Job();
        mapRequestToJob(request, job);
        job.setRecruiter(recruiter);
        job.setCompanyName(profile.getCompanyName());
        job.setStatus(JobStatus.OPEN);

        if (request.getRequiredSkills() != null) {
            job.setRequiredSkills(resolveSkills(request.getRequiredSkills()));
        }

        Job savedJob = jobRepository.save(job);
        return EntityMapper.toJobResponse(savedJob);
    }

    @Override
    @Transactional
    public JobResponse updateJob(Long jobId, JobRequest request, String recruiterEmail) {
        Job job = getJobAndVerifyOwnership(jobId, recruiterEmail);

        if (job.getStatus() == JobStatus.CLOSED) {
            throw new BusinessRuleException("Cannot update a closed job");
        }

        mapRequestToJob(request, job);
        if (request.getRequiredSkills() != null) {
            job.setRequiredSkills(resolveSkills(request.getRequiredSkills()));
        }

        Job updatedJob = jobRepository.save(job);
        return EntityMapper.toJobResponse(updatedJob);
    }

    @Override
    @Transactional(readOnly = true)
    public JobResponse getJobById(Long jobId) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));
        return EntityMapper.toJobResponse(job);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> searchJobs(String keyword, String location, String employmentType,
                                         Integer experience, BigDecimal salaryMin, Pageable pageable) {
        EmploymentType empType = null;
        if (employmentType != null && !employmentType.isBlank()) {
            try {
                empType = EmploymentType.valueOf(employmentType.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessRuleException("Invalid employment type: " + employmentType);
            }
        }

        String kw = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;
        String loc = (location != null && !location.isBlank()) ? location.trim() : null;

        return jobRepository.findAll(
                JobSpecifications.filterJobs(kw, loc, empType, experience, salaryMin),
                pageable
        ).map(EntityMapper::toJobResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<JobResponse> getRecruiterJobs(String recruiterEmail, String status, Pageable pageable) {
        User recruiter = getUserByEmail(recruiterEmail);

        if (status != null && !status.isBlank()) {
            JobStatus jobStatus = JobStatus.valueOf(status.toUpperCase());
            return jobRepository.findByRecruiterIdAndStatus(recruiter.getId(), jobStatus, pageable)
                    .map(EntityMapper::toJobResponse);
        }

        return jobRepository.findByRecruiterId(recruiter.getId(), pageable)
                .map(EntityMapper::toJobResponse);
    }

    @Override
    @Transactional
    public void closeJob(Long jobId, String recruiterEmail) {
        Job job = getJobAndVerifyOwnership(jobId, recruiterEmail);
        job.setStatus(JobStatus.CLOSED);
        jobRepository.save(job);
    }

    @Override
    @Transactional
    public void deleteJob(Long jobId, String userEmail, boolean isAdmin) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!isAdmin && !job.getRecruiter().getEmail().equals(userEmail)) {
            throw new BusinessRuleException("You can only delete your own jobs");
        }

        jobRepository.delete(job);
    }

    private Job getJobAndVerifyOwnership(Long jobId, String recruiterEmail) {
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found with id: " + jobId));

        if (!job.getRecruiter().getEmail().equals(recruiterEmail)) {
            throw new BusinessRuleException("You can only modify your own jobs");
        }

        return job;
    }

    private void mapRequestToJob(JobRequest request, Job job) {
        job.setTitle(request.getTitle().trim());
        job.setDescription(request.getDescription().trim());
        job.setLocation(request.getLocation().trim());
        job.setEmploymentType(EmploymentType.valueOf(request.getEmploymentType().toUpperCase()));
        job.setExperienceMin(request.getExperienceMin());
        job.setExperienceMax(request.getExperienceMax());
        job.setSalaryMin(request.getSalaryMin());
        job.setSalaryMax(request.getSalaryMax());
        job.setDeadline(request.getDeadline());
    }

    private Set<Skill> resolveSkills(Set<String> skillNames) {
        Set<Skill> skills = new HashSet<>();
        for (String name : skillNames) {
            String trimmed = name.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                Skill skill = skillRepository.findByNameIgnoreCase(trimmed)
                        .orElseGet(() -> skillRepository.save(new Skill(trimmed)));
                skills.add(skill);
            }
        }
        return skills;
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
