package com.jobrecruitment.controller;

import com.jobrecruitment.dto.request.ProfileUpdateRequest;
import com.jobrecruitment.dto.response.CandidateProfileResponse;
import com.jobrecruitment.dto.response.DashboardStats;
import com.jobrecruitment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/candidate")
@Tag(name = "Candidate", description = "Candidate Profile and Dashboard APIs")
public class CandidateController {

    private final UserService userService;

    public CandidateController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    @Operation(summary = "Get candidate profile")
    public ResponseEntity<CandidateProfileResponse> getProfile(Authentication authentication) {
        return ResponseEntity.ok(userService.getCandidateProfile(authentication.getName()));
    }

    @PutMapping("/profile")
    @Operation(summary = "Update candidate profile")
    public ResponseEntity<CandidateProfileResponse> updateProfile(
            @Valid @RequestBody ProfileUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(userService.updateCandidateProfile(authentication.getName(), request));
    }

    @PostMapping("/skills")
    @Operation(summary = "Add skills to profile")
    public ResponseEntity<CandidateProfileResponse> addSkills(
            @RequestBody Set<String> skills,
            Authentication authentication) {
        return ResponseEntity.ok(userService.addSkills(authentication.getName(), skills));
    }

    @DeleteMapping("/skills/{skillName}")
    @Operation(summary = "Remove a skill from profile")
    public ResponseEntity<CandidateProfileResponse> removeSkill(
            @PathVariable String skillName,
            Authentication authentication) {
        return ResponseEntity.ok(userService.removeSkill(authentication.getName(), skillName));
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get candidate dashboard statistics")
    public ResponseEntity<DashboardStats> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(userService.getCandidateDashboard(authentication.getName()));
    }

    @PostMapping(value = "/resume", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload resume")
    public ResponseEntity<CandidateProfileResponse> uploadResume(
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file,
            Authentication authentication) {
        return ResponseEntity.ok(userService.uploadResume(authentication.getName(), file));
    }

    @GetMapping("/resume")
    @Operation(summary = "Download my resume")
    public ResponseEntity<org.springframework.core.io.Resource> getMyResume(Authentication authentication) {
        CandidateProfileResponse profile = userService.getCandidateProfile(authentication.getName());
        org.springframework.core.io.Resource resource = userService.getResume(profile.getUserId());
        String filename = userService.getResumeFileName(profile.getUserId());
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }
}
