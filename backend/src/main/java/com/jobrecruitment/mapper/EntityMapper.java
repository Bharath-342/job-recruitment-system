package com.jobrecruitment.mapper;

import com.jobrecruitment.dto.response.*;
import com.jobrecruitment.entity.*;

import java.util.stream.Collectors;

public class EntityMapper {

    private EntityMapper() {}

    public static UserResponse toUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setFirstName(user.getFirstName());
        response.setLastName(user.getLastName());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole().name());
        response.setActive(user.isActive());
        response.setCreatedAt(user.getCreatedAt());
        return response;
    }

    public static JobResponse toJobResponse(Job job) {
        JobResponse response = new JobResponse();
        response.setId(job.getId());
        response.setTitle(job.getTitle());
        response.setDescription(job.getDescription());
        response.setLocation(job.getLocation());
        response.setEmploymentType(job.getEmploymentType().name());
        response.setExperienceMin(job.getExperienceMin());
        response.setExperienceMax(job.getExperienceMax());
        response.setSalaryMin(job.getSalaryMin());
        response.setSalaryMax(job.getSalaryMax());
        response.setStatus(job.getStatus().name());
        response.setCompanyName(job.getCompanyName());
        response.setRecruiterName(job.getRecruiter().getFullName());
        response.setRecruiterId(job.getRecruiter().getId());
        response.setDeadline(job.getDeadline());
        response.setCreatedAt(job.getCreatedAt());

        if (job.getRequiredSkills() != null) {
            response.setRequiredSkills(
                job.getRequiredSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.toSet())
            );
        }

        return response;
    }

    public static ApplicationResponse toApplicationResponse(Application application) {
        ApplicationResponse response = new ApplicationResponse();
        response.setId(application.getId());
        response.setJobId(application.getJob().getId());
        response.setJobTitle(application.getJob().getTitle());
        response.setCompanyName(application.getJob().getCompanyName());
        response.setLocation(application.getJob().getLocation());
        response.setCandidateId(application.getCandidate().getId());
        response.setCandidateName(application.getCandidate().getFullName());
        response.setCandidateEmail(application.getCandidate().getEmail());
        response.setStatus(application.getStatus().name());
        response.setCoverLetter(application.getCoverLetter());
        response.setRecruiterNotes(application.getRecruiterNotes());
        response.setAppliedAt(application.getAppliedAt());
        response.setUpdatedAt(application.getUpdatedAt());
        return response;
    }

    public static CandidateProfileResponse toCandidateProfileResponse(CandidateProfile profile) {
        CandidateProfileResponse response = new CandidateProfileResponse();
        response.setId(profile.getId());
        response.setUserId(profile.getUser().getId());
        response.setFirstName(profile.getUser().getFirstName());
        response.setLastName(profile.getUser().getLastName());
        response.setEmail(profile.getUser().getEmail());
        response.setHeadline(profile.getHeadline());
        response.setSummary(profile.getSummary());
        response.setPhone(profile.getPhone());
        response.setLocation(profile.getLocation());
        response.setExperienceYears(profile.getExperienceYears());
        response.setResumeFileName(profile.getResumeFileName());
        response.setLinkedinUrl(profile.getLinkedinUrl());
        response.setGithubUrl(profile.getGithubUrl());

        if (profile.getSkills() != null) {
            response.setSkills(
                profile.getSkills().stream()
                    .map(Skill::getName)
                    .collect(Collectors.toSet())
            );
        }

        return response;
    }
}
