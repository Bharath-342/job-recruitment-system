package com.jobrecruitment.service.impl;

import com.jobrecruitment.dto.request.ProfileUpdateRequest;
import com.jobrecruitment.dto.response.CandidateProfileResponse;
import com.jobrecruitment.dto.response.DashboardStats;
import com.jobrecruitment.dto.response.UserResponse;
import com.jobrecruitment.entity.*;
import com.jobrecruitment.exception.ResourceNotFoundException;
import com.jobrecruitment.mapper.EntityMapper;
import com.jobrecruitment.repository.*;
import com.jobrecruitment.service.UserService;
import com.jobrecruitment.exception.BusinessRuleException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Value("${upload.dir:./uploads}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final SkillRepository skillRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;

    public UserServiceImpl(UserRepository userRepository,
                           CandidateProfileRepository candidateProfileRepository,
                           RecruiterProfileRepository recruiterProfileRepository,
                           SkillRepository skillRepository,
                           JobRepository jobRepository,
                           ApplicationRepository applicationRepository) {
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.skillRepository = skillRepository;
        this.jobRepository = jobRepository;
        this.applicationRepository = applicationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateProfileResponse getCandidateProfile(String email) {
        User user = getUserByEmail(email);
        CandidateProfile profile = candidateProfileRepository.findByUserIdWithSkills(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        return EntityMapper.toCandidateProfileResponse(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public CandidateProfileResponse getCandidateProfileById(Long userId) {
        CandidateProfile profile = candidateProfileRepository.findByUserIdWithSkills(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        return EntityMapper.toCandidateProfileResponse(profile);
    }

    @Override
    @Transactional
    public CandidateProfileResponse updateCandidateProfile(String email, ProfileUpdateRequest request) {
        User user = getUserByEmail(email);
        CandidateProfile profile = candidateProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        if (request.getHeadline() != null) profile.setHeadline(request.getHeadline());
        if (request.getSummary() != null) profile.setSummary(request.getSummary());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getExperienceYears() != null) profile.setExperienceYears(request.getExperienceYears());
        if (request.getLinkedinUrl() != null) profile.setLinkedinUrl(request.getLinkedinUrl());
        if (request.getGithubUrl() != null) profile.setGithubUrl(request.getGithubUrl());

        CandidateProfile saved = candidateProfileRepository.save(profile);
        return EntityMapper.toCandidateProfileResponse(saved);
    }

    @Override
    @Transactional
    public CandidateProfileResponse addSkills(String email, Set<String> skillNames) {
        User user = getUserByEmail(email);
        CandidateProfile profile = candidateProfileRepository.findByUserIdWithSkills(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        for (String name : skillNames) {
            String trimmed = name.trim().toLowerCase();
            if (!trimmed.isEmpty()) {
                Skill skill = skillRepository.findByNameIgnoreCase(trimmed)
                        .orElseGet(() -> skillRepository.save(new Skill(trimmed)));
                profile.getSkills().add(skill);
            }
        }

        CandidateProfile saved = candidateProfileRepository.save(profile);
        return EntityMapper.toCandidateProfileResponse(saved);
    }

    @Override
    @Transactional
    public CandidateProfileResponse removeSkill(String email, String skillName) {
        User user = getUserByEmail(email);
        CandidateProfile profile = candidateProfileRepository.findByUserIdWithSkills(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        profile.getSkills().removeIf(skill -> skill.getName().equalsIgnoreCase(skillName.trim()));
        CandidateProfile saved = candidateProfileRepository.save(profile);
        return EntityMapper.toCandidateProfileResponse(saved);
    }

    @Override
    @Transactional
    public void updateRecruiterProfile(String email, ProfileUpdateRequest request) {
        User user = getUserByEmail(email);
        RecruiterProfile profile = recruiterProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Recruiter profile not found"));

        if (request.getCompanyName() != null) profile.setCompanyName(request.getCompanyName());
        if (request.getDesignation() != null) profile.setDesignation(request.getDesignation());
        if (request.getPhone() != null) profile.setPhone(request.getPhone());
        if (request.getLocation() != null) profile.setLocation(request.getLocation());
        if (request.getCompanyWebsite() != null) profile.setCompanyWebsite(request.getCompanyWebsite());
        if (request.getCompanyDescription() != null) profile.setCompanyDescription(request.getCompanyDescription());

        recruiterProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(EntityMapper::toUserResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(String role, Pageable pageable) {
        Role r = Role.valueOf(role.toUpperCase());
        return userRepository.findByRole(r, pageable).map(EntityMapper::toUserResponse);
    }

    @Override
    @Transactional
    public void toggleUserActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getCandidateDashboard(String email) {
        User user = getUserByEmail(email);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalApplications", applicationRepository.countByCandidateId(user.getId()));
        stats.put("applied", applicationRepository.countByCandidateIdAndStatus(user.getId(), ApplicationStatus.APPLIED));
        stats.put("underReview", applicationRepository.countByCandidateIdAndStatus(user.getId(), ApplicationStatus.UNDER_REVIEW));
        stats.put("shortlisted", applicationRepository.countByCandidateIdAndStatus(user.getId(), ApplicationStatus.SHORTLISTED));
        stats.put("interviews", applicationRepository.countByCandidateIdAndStatus(user.getId(), ApplicationStatus.INTERVIEW));
        stats.put("selected", applicationRepository.countByCandidateIdAndStatus(user.getId(), ApplicationStatus.SELECTED));
        stats.put("rejected", applicationRepository.countByCandidateIdAndStatus(user.getId(), ApplicationStatus.REJECTED));
        return new DashboardStats(stats);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getRecruiterDashboard(String email) {
        User user = getUserByEmail(email);
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("activeJobs", jobRepository.countByRecruiterIdAndStatus(user.getId(), JobStatus.OPEN));
        stats.put("closedJobs", jobRepository.countByRecruiterIdAndStatus(user.getId(), JobStatus.CLOSED));
        stats.put("totalApplications", applicationRepository.countByJobRecruiterId(user.getId()));
        stats.put("shortlisted", applicationRepository.countByJobRecruiterIdAndStatus(user.getId(), ApplicationStatus.SHORTLISTED));
        stats.put("interviews", applicationRepository.countByJobRecruiterIdAndStatus(user.getId(), ApplicationStatus.INTERVIEW));
        stats.put("selected", applicationRepository.countByJobRecruiterIdAndStatus(user.getId(), ApplicationStatus.SELECTED));
        stats.put("rejected", applicationRepository.countByJobRecruiterIdAndStatus(user.getId(), ApplicationStatus.REJECTED));
        return new DashboardStats(stats);
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardStats getAdminDashboard() {
        Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalCandidates", userRepository.countByRole(Role.CANDIDATE));
        stats.put("totalRecruiters", userRepository.countByRole(Role.RECRUITER));
        stats.put("activeUsers", userRepository.countByActive(true));
        stats.put("totalJobs", jobRepository.count());
        stats.put("openJobs", jobRepository.countByStatus(JobStatus.OPEN));
        stats.put("closedJobs", jobRepository.countByStatus(JobStatus.CLOSED));
        stats.put("totalApplications", applicationRepository.count());
        stats.put("selectedCandidates", applicationRepository.countByStatus(ApplicationStatus.SELECTED));
        return new DashboardStats(stats);
    }

    @Override
    @Transactional
    public CandidateProfileResponse uploadResume(String email, MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessRuleException("Cannot upload an empty file");
        }

        User user = getUserByEmail(email);
        CandidateProfile profile = candidateProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        try {
            Path directory = Paths.get(uploadDir).toAbsolutePath().normalize();
            Files.createDirectories(directory);

            String originalFilename = file.getOriginalFilename();
            if (originalFilename == null || originalFilename.isBlank()) {
                originalFilename = "resume.pdf";
            }
            // Sanitize filename
            String cleanName = Paths.get(originalFilename).getFileName().toString();
            String storedFileName = user.getId() + "_" + UUID.randomUUID() + "_" + cleanName;
            Path targetPath = directory.resolve(storedFileName);

            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            profile.setResumeFileName(cleanName);
            profile.setResumeFilePath(targetPath.toString());
            candidateProfileRepository.save(profile);

            return EntityMapper.toCandidateProfileResponse(profile);
        } catch (IOException ex) {
            throw new BusinessRuleException("Could not store file: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Resource getResume(Long userId) {
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));

        if (profile.getResumeFilePath() == null || profile.getResumeFilePath().isBlank()) {
            throw new ResourceNotFoundException("Resume not uploaded for this candidate");
        }

        try {
            Path filePath = Paths.get(profile.getResumeFilePath()).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new ResourceNotFoundException("Resume file not found on server");
            }
        } catch (Exception ex) {
            throw new ResourceNotFoundException("Could not read resume file: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public String getResumeFileName(Long userId) {
        CandidateProfile profile = candidateProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Candidate profile not found"));
        return profile.getResumeFileName() != null ? profile.getResumeFileName() : "resume.pdf";
    }

    private User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}
