package com.jobrecruitment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrecruitment.dto.request.JobRequest;
import com.jobrecruitment.dto.request.LoginRequest;
import com.jobrecruitment.dto.response.AuthResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class JobControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String recruiterToken;
    private String candidateToken;

    @BeforeEach
    void setupTokens() throws Exception {
        // Login as recruiter (seeded)
        LoginRequest recLogin = new LoginRequest("recruiter@techcorp.com", "Recruiter@123");
        MvcResult recResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recLogin)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse recAuth = objectMapper.readValue(recResult.getResponse().getContentAsString(), AuthResponse.class);
        recruiterToken = recAuth.getToken();

        // Login as candidate (seeded)
        LoginRequest candLogin = new LoginRequest("candidate@dev.com", "Candidate@123");
        MvcResult candResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candLogin)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse candAuth = objectMapper.readValue(candResult.getResponse().getContentAsString(), AuthResponse.class);
        candidateToken = candAuth.getToken();
    }

    @Test
    @DisplayName("GET /api/jobs - Public browse jobs with pagination")
    void searchJobs_PublicAccess() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(1)));
    }

    @Test
    @DisplayName("GET /api/jobs - Filter with keyword and location")
    void searchJobs_FilterKeyword() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "Java")
                        .param("location", "Hyderabad"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/jobs - Filter with non-existent keyword returns empty page")
    void searchJobs_EmptyResults() throws Exception {
        mockMvc.perform(get("/api/jobs")
                        .param("keyword", "XYZNonExistentRole999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements", is(0)));
    }

    @Test
    @DisplayName("GET /api/jobs/{id} - Non-existent job returns 404")
    void getJob_NotFound() throws Exception {
        mockMvc.perform(get("/api/jobs/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("NOT_FOUND")));
    }

    @Test
    @DisplayName("POST /api/jobs - Unauthorized when no token provided")
    void createJob_Unauthorized() throws Exception {
        JobRequest request = new JobRequest();
        request.setTitle("Security Test Job");
        request.setDescription("Job description for security test");
        request.setLocation("Remote");
        request.setEmploymentType("FULL_TIME");

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /api/jobs - Forbidden when called by Candidate")
    void createJob_ForbiddenForCandidate() throws Exception {
        JobRequest request = new JobRequest();
        request.setTitle("Security Test Job");
        request.setDescription("Job description for security test");
        request.setLocation("Remote");
        request.setEmploymentType("FULL_TIME");

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/jobs - Success when called by Recruiter")
    void createJob_SuccessForRecruiter() throws Exception {
        JobRequest request = new JobRequest();
        request.setTitle("Lead Architect");
        request.setDescription("Comprehensive job description for Lead Architect position.");
        request.setLocation("Hyderabad, India");
        request.setEmploymentType("FULL_TIME");
        request.setExperienceMin(5);
        request.setExperienceMax(10);
        request.setSalaryMin(new BigDecimal("2500000"));
        request.setSalaryMax(new BigDecimal("4000000"));
        request.setRequiredSkills(Set.of("Java", "Spring Boot", "Microservices"));

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title", is("Lead Architect")))
                .andExpect(jsonPath("$.status", is("OPEN")));
    }

    @Test
    @DisplayName("POST /api/jobs - Validation failure when title is missing")
    void createJob_ValidationFailure() throws Exception {
        JobRequest request = new JobRequest();
        // Title missing, description too short
        request.setDescription("short");

        mockMvc.perform(post("/api/jobs")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }
}
