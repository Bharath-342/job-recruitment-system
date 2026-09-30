package com.jobrecruitment.controller;

import com.jobrecruitment.dto.response.DashboardStats;
import com.jobrecruitment.dto.response.UserResponse;
import com.jobrecruitment.service.JobService;
import com.jobrecruitment.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Admin Management APIs")
public class AdminController {

    private final UserService userService;
    private final JobService jobService;

    public AdminController(UserService userService, JobService jobService) {
        this.userService = userService;
        this.jobService = jobService;
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users")
    public ResponseEntity<Page<UserResponse>> getUsers(
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (role != null && !role.isBlank()) {
            return ResponseEntity.ok(userService.getUsersByRole(role,
                    PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
        }
        return ResponseEntity.ok(userService.getAllUsers(
                PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))));
    }

    @PatchMapping("/users/{id}/toggle-active")
    @Operation(summary = "Activate/Deactivate a user")
    public ResponseEntity<Void> toggleUserActive(@PathVariable Long id) {
        userService.toggleUserActive(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/jobs/{id}")
    @Operation(summary = "Remove a job (Admin)")
    public ResponseEntity<Void> removeJob(@PathVariable Long id, Authentication authentication) {
        jobService.deleteJob(id, authentication.getName(), true);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get admin dashboard statistics")
    public ResponseEntity<DashboardStats> getStatistics() {
        return ResponseEntity.ok(userService.getAdminDashboard());
    }
}
