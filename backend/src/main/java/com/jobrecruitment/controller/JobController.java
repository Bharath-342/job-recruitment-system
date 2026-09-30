package com.jobrecruitment.controller;

import com.jobrecruitment.dto.request.JobRequest;
import com.jobrecruitment.dto.response.JobResponse;
import com.jobrecruitment.service.JobService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Jobs", description = "Job Management APIs")
public class JobController {

    private final JobService jobService;

    public JobController(JobService jobService) {
        this.jobService = jobService;
    }

    @GetMapping
    @Operation(summary = "Search and browse jobs (public)")
    public ResponseEntity<Page<JobResponse>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String employmentType,
            @RequestParam(required = false) Integer experience,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        return ResponseEntity.ok(jobService.searchJobs(keyword, location, employmentType,
                experience, salaryMin, pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get job details (public)")
    public ResponseEntity<JobResponse> getJob(@PathVariable Long id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PostMapping
    @Operation(summary = "Create a new job (Recruiter only)")
    public ResponseEntity<JobResponse> createJob(@Valid @RequestBody JobRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(jobService.createJob(request, authentication.getName()));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a job (Recruiter only)")
    public ResponseEntity<JobResponse> updateJob(@PathVariable Long id,
                                                  @Valid @RequestBody JobRequest request,
                                                  Authentication authentication) {
        return ResponseEntity.ok(jobService.updateJob(id, request, authentication.getName()));
    }

    @PatchMapping("/{id}/close")
    @Operation(summary = "Close a job (Recruiter only)")
    public ResponseEntity<Void> closeJob(@PathVariable Long id, Authentication authentication) {
        jobService.closeJob(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a job (Recruiter owner or Admin)")
    public ResponseEntity<Void> deleteJob(@PathVariable Long id, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        jobService.deleteJob(id, authentication.getName(), isAdmin);
        return ResponseEntity.noContent().build();
    }
}
