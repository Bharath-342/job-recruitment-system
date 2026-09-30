package com.jobrecruitment.controller;

import com.jobrecruitment.dto.response.*;
import com.jobrecruitment.entity.ExperienceLevel;
import com.jobrecruitment.service.JobAggregationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@Tag(name = "Fresher Jobs", description = "Real-time Fresher Job Aggregation & Discovery APIs")
public class FresherJobController {

    private final JobAggregationService aggregationService;

    public FresherJobController(JobAggregationService aggregationService) {
        this.aggregationService = aggregationService;
    }

    @GetMapping("/fresher")
    @Operation(summary = "Search fresher and entry-level jobs with multi-criteria filters")
    public ResponseEntity<Page<FresherJobResponse>> searchFresherJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        String safeSort = (sort == null || sort.isBlank() || sort.equalsIgnoreCase("createdAt")) ? "postedAt" : sort;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, safeSort));

        return ResponseEntity.ok(aggregationService.searchFresherJobs(
                keyword, location, role, company, remote, experienceLevel, pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Unified search across aggregated jobs")
    public ResponseEntity<Page<FresherJobResponse>> searchAllAggregatedJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String company,
            @RequestParam(required = false) Boolean remote,
            @RequestParam(required = false) ExperienceLevel experienceLevel,
            @RequestParam(required = false) Boolean isFresher,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "postedAt") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort.Direction dir = direction.equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        String safeSort = (sort == null || sort.isBlank() || sort.equalsIgnoreCase("createdAt")) ? "postedAt" : sort;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, safeSort));

        return ResponseEntity.ok(aggregationService.searchAllAggregatedJobs(
                keyword, location, role, company, remote, experienceLevel, isFresher, pageable));
    }

    @GetMapping("/fresher/{id}")
    @Operation(summary = "Get single fresher job details by ID")
    public ResponseEntity<FresherJobResponse> getFresherJobById(@PathVariable Long id) {
        return ResponseEntity.ok(aggregationService.getJobById(id));
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get verified fresher job statistics and company counts")
    public ResponseEntity<JobStatisticsResponse> getStatistics() {
        return ResponseEntity.ok(aggregationService.getStatistics());
    }

    @GetMapping("/companies")
    @Operation(summary = "Get unique companies currently hiring freshers")
    public ResponseEntity<List<String>> getCompaniesHiringFreshers() {
        return ResponseEntity.ok(aggregationService.getCompaniesHiringFreshers());
    }

    @GetMapping("/sources")
    @Operation(summary = "Get all configured job aggregation sources and statuses")
    public ResponseEntity<List<CompanySourceDto>> getAllSources() {
        return ResponseEntity.ok(aggregationService.getAllSources());
    }

    @PostMapping("/sync")
    @Operation(summary = "Trigger job synchronization (Admin or authorized)")
    public ResponseEntity<SyncSummaryDto> triggerSync(
            @RequestParam(required = false) Long sourceId) {
        if (sourceId != null) {
            return ResponseEntity.ok(aggregationService.syncSource(sourceId));
        }
        return ResponseEntity.ok(aggregationService.syncAllSources());
    }
}
