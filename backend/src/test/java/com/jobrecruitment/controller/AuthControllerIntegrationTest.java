package com.jobrecruitment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobrecruitment.dto.request.LoginRequest;
import com.jobrecruitment.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/auth/register/candidate - Success")
    void registerCandidate_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Alice");
        request.setLastName("Wonder");
        request.setEmail("alice.unique." + System.currentTimeMillis() + "@test.com");
        request.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.email", is(request.getEmail())))
                .andExpect(jsonPath("$.role", is("CANDIDATE")));
    }

    @Test
    @DisplayName("POST /api/auth/register/candidate - Duplicate Email Edge Case")
    void registerCandidate_DuplicateEmail() throws Exception {
        String email = "dup." + System.currentTimeMillis() + "@test.com";

        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Bob");
        request.setLastName("Smith");
        request.setEmail(email);
        request.setPassword("Password@123");

        // First registration
        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Second registration with same email
        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", is("CONFLICT")));
    }

    @Test
    @DisplayName("POST /api/auth/register/candidate - Validation Failure: Missing Fields")
    void registerCandidate_ValidationFailure() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("not-an-email"); // invalid email format, missing password & names

        mockMvc.perform(post("/api/auth/register/candidate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("VALIDATION_ERROR")));
    }

    @Test
    @DisplayName("POST /api/auth/register/recruiter - Success")
    void registerRecruiter_Success() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFirstName("Robert");
        request.setLastName("Partner");
        request.setEmail("robert.recruiter." + System.currentTimeMillis() + "@test.com");
        request.setPassword("Recruiter@123");
        request.setCompanyName("Acme Talent Inc");

        mockMvc.perform(post("/api/auth/register/recruiter")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role", is("RECRUITER")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Success with Seeded Admin")
    void login_Success() throws Exception {
        LoginRequest request = new LoginRequest("admin@jobrecruitment.com", "Admin@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role", is("ADMIN")));
    }

    @Test
    @DisplayName("POST /api/auth/login - Invalid Password Edge Case")
    void login_InvalidPassword() throws Exception {
        LoginRequest request = new LoginRequest("admin@jobrecruitment.com", "WrongPassword!999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
