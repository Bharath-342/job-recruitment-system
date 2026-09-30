package com.jobrecruitment.service;

import com.jobrecruitment.dto.request.ProfileUpdateRequest;
import com.jobrecruitment.dto.response.CandidateProfileResponse;
import com.jobrecruitment.dto.response.DashboardStats;
import com.jobrecruitment.dto.response.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface UserService {
    CandidateProfileResponse getCandidateProfile(String email);
    CandidateProfileResponse getCandidateProfileById(Long userId);
    CandidateProfileResponse updateCandidateProfile(String email, ProfileUpdateRequest request);
    CandidateProfileResponse addSkills(String email, Set<String> skillNames);
    CandidateProfileResponse removeSkill(String email, String skillName);
    void updateRecruiterProfile(String email, ProfileUpdateRequest request);
    Page<UserResponse> getAllUsers(Pageable pageable);
    Page<UserResponse> getUsersByRole(String role, Pageable pageable);
    void toggleUserActive(Long userId);
    DashboardStats getCandidateDashboard(String email);
    DashboardStats getRecruiterDashboard(String email);
    DashboardStats getAdminDashboard();
    CandidateProfileResponse uploadResume(String email, org.springframework.web.multipart.MultipartFile file);
    org.springframework.core.io.Resource getResume(Long userId);
    String getResumeFileName(Long userId);
}
