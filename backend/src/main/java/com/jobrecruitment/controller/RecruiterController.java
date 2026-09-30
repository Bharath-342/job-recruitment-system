package com.jobrecruitment.controller;

import com.jobrecruitment.dto.request.ProfileUpdateRequest;
import com.jobrecruitment.dto.request.StatusUpdateRequest;
import com.jobrecruitment.dto.response.ApplicationResponse;
import com.jobrecruitment.dto.response.CandidateProfileResponse;
import com.jobrecruitment.dto.response.DashboardStats;
import com.jobrecruitment.dto.response.JobResponse;
import com.jobrecruitment.service.ApplicationService;
import com.jobrecruitment.service.JobService;
import com.jobrecruitment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/recruiter")
@Tag(name = "Recruiter", description = "Recruiter Management APIs")
public class RecruiterController {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserService userService;

    public RecruiterController(JobService jobService, ApplicationService applicationService,
                                UserService userService) {
        this.jobService = jobService;
        this.applicationService = applicationService;
        this.userService = userService;
    }

    @GetMapping("/jobs")
    @Operation(summary = "Get recruiter's jobs")
    public ResponseEntity<Page<JobResponse>> getMyJobs(
            Authentication authentication,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(jobService.getRecruiterJobs(
                authentication.getName(), status, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @GetMapping("/applications")
    @Operation(summary = "Get all applications for recruiter's jobs")
    public ResponseEntity<Page<ApplicationResponse>> getApplications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getRecruiterApplications(
                authentication.getName(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"))));
    }

    @GetMapping("/jobs/{jobId}/applications")
    @Operation(summary = "Get applications for a specific job")
    public ResponseEntity<Page<ApplicationResponse>> getJobApplications(
            @PathVariable Long jobId,
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getApplicationsByJob(
                jobId, authentication.getName(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"))));
    }

    @PatchMapping("/applications/{id}/status")
    @Operation(summary = "Update application status")
    public ResponseEntity<ApplicationResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request,
            Authentication authentication) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(
                id, request, authentication.getName()));
    }

    @GetMapping("/candidates/{userId}")
    @Operation(summary = "View candidate profile")
    public ResponseEntity<CandidateProfileResponse> getCandidateProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getCandidateProfileById(userId));
    }

    @GetMapping("/candidates/{userId}/resume")
    @Operation(summary = "Download candidate resume")
    public ResponseEntity<org.springframework.core.io.Resource> getCandidateResume(@PathVariable Long userId) {
        org.springframework.core.io.Resource resource = userService.getResume(userId);
        String filename = userService.getResumeFileName(userId);
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(resource);
    }

    @PutMapping("/profile")
    @Operation(summary = "Update recruiter profile")
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody ProfileUpdateRequest request,
                                               Authentication authentication) {
        userService.updateRecruiterProfile(authentication.getName(), request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Get recruiter dashboard statistics")
    public ResponseEntity<DashboardStats> getDashboard(Authentication authentication) {
        return ResponseEntity.ok(userService.getRecruiterDashboard(authentication.getName()));
    }
}
