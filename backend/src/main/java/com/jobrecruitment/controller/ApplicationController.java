package com.jobrecruitment.controller;

import com.jobrecruitment.dto.request.ApplicationRequest;
import com.jobrecruitment.dto.response.ApplicationResponse;
import com.jobrecruitment.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@Tag(name = "Applications", description = "Job Application APIs")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping("/jobs/{jobId}/applications")
    @Operation(summary = "Apply for a job (Candidate only)")
    public ResponseEntity<ApplicationResponse> applyForJob(
            @PathVariable Long jobId,
            @RequestBody(required = false) ApplicationRequest request,
            Authentication authentication) {
        if (request == null) request = new ApplicationRequest();
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(applicationService.applyForJob(jobId, request, authentication.getName()));
    }

    @GetMapping("/applications/my")
    @Operation(summary = "Get my applications (Candidate only)")
    public ResponseEntity<Page<ApplicationResponse>> getMyApplications(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(applicationService.getCandidateApplications(
                authentication.getName(), PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "appliedAt"))));
    }

    @DeleteMapping("/applications/{id}/withdraw")
    @Operation(summary = "Withdraw an application (Candidate only)")
    public ResponseEntity<Void> withdrawApplication(@PathVariable Long id, Authentication authentication) {
        applicationService.withdrawApplication(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
