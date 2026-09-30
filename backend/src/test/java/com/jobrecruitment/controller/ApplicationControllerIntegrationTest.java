package com.jobrecruitment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrecruitment.dto.request.ApplicationRequest;
import com.jobrecruitment.dto.request.LoginRequest;
import com.jobrecruitment.dto.request.StatusUpdateRequest;
import com.jobrecruitment.dto.response.AuthResponse;
import com.jobrecruitment.entity.Job;
import com.jobrecruitment.repository.JobRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApplicationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JobRepository jobRepository;

    private String candidateToken;
    private String recruiterToken;
    private Long testJobId;

    @BeforeEach
    void setUp() throws Exception {
        LoginRequest candLogin = new LoginRequest("candidate@dev.com", "Candidate@123");
        MvcResult candResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(candLogin)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse candAuth = objectMapper.readValue(candResult.getResponse().getContentAsString(), AuthResponse.class);
        candidateToken = candAuth.getToken();

        LoginRequest recLogin = new LoginRequest("recruiter@techcorp.com", "Recruiter@123");
        MvcResult recResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(recLogin)))
                .andExpect(status().isOk())
                .andReturn();
        AuthResponse recAuth = objectMapper.readValue(recResult.getResponse().getContentAsString(), AuthResponse.class);
        recruiterToken = recAuth.getToken();

        // Get an open job ID from repository
        Job job = jobRepository.findAll().stream().findFirst().orElseThrow();
        testJobId = job.getId();
    }

    @Test
    @DisplayName("GET /api/applications/my - Candidate gets submitted applications")
    void getMyApplications_Success() throws Exception {
        mockMvc.perform(get("/api/applications/my")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/jobs/{id}/applications - Forbidden for Recruiter")
    void applyForJob_ForbiddenForRecruiter() throws Exception {
        ApplicationRequest req = new ApplicationRequest();
        req.setCoverLetter("Recruiter trying to apply");

        mockMvc.perform(post("/api/jobs/" + testJobId + "/applications")
                        .header("Authorization", "Bearer " + recruiterToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/jobs/{id}/applications - Duplicate Application Edge Case")
    void applyForJob_DuplicatePrevention() throws Exception {
        // Candidate is already applied to Job 1 from DataInitializer
        ApplicationRequest req = new ApplicationRequest();
        req.setCoverLetter("Trying to apply again");

        mockMvc.perform(post("/api/jobs/" + testJobId + "/applications")
                        .header("Authorization", "Bearer " + candidateToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")));
    }

    @Test
    @DisplayName("GET /api/admin/statistics - Protected Admin statistics")
    void getAdminStatistics_ForbiddenForCandidate() throws Exception {
        mockMvc.perform(get("/api/admin/statistics")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/candidate/dashboard - Candidate stats returned")
    void getCandidateDashboard_Success() throws Exception {
        mockMvc.perform(get("/api/candidate/dashboard")
                        .header("Authorization", "Bearer " + candidateToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.stats", notNullValue()));
    }
}
