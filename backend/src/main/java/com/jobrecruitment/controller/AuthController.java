package com.jobrecruitment.controller;

import com.jobrecruitment.dto.request.LoginRequest;
import com.jobrecruitment.dto.request.RegisterRequest;
import com.jobrecruitment.dto.response.AuthResponse;
import com.jobrecruitment.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Registration and Login APIs")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register/candidate")
    @Operation(summary = "Register as a Candidate")
    public ResponseEntity<AuthResponse> registerCandidate(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerCandidate(request));
    }

    @PostMapping("/register/recruiter")
    @Operation(summary = "Register as a Recruiter")
    public ResponseEntity<AuthResponse> registerRecruiter(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerRecruiter(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
