package com.jobrecruitment.service;

import com.jobrecruitment.dto.request.LoginRequest;
import com.jobrecruitment.dto.request.RegisterRequest;
import com.jobrecruitment.dto.response.AuthResponse;
import com.jobrecruitment.entity.Role;
import com.jobrecruitment.entity.User;
import com.jobrecruitment.exception.DuplicateResourceException;
import com.jobrecruitment.repository.CandidateProfileRepository;
import com.jobrecruitment.repository.RecruiterProfileRepository;
import com.jobrecruitment.repository.UserRepository;
import com.jobrecruitment.security.JwtTokenProvider;
import com.jobrecruitment.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private CandidateProfileRepository candidateProfileRepository;
    @Mock private RecruiterProfileRepository recruiterProfileRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private AuthenticationManager authenticationManager;

    @InjectMocks private AuthServiceImpl authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("Password123");
    }

    @Test
    @DisplayName("Should register candidate successfully")
    void registerCandidate_Success() {
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("test-token");

        AuthResponse response = authService.registerCandidate(registerRequest);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("CANDIDATE", response.getRole());
        verify(userRepository).save(any(User.class));
        verify(candidateProfileRepository).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email exists")
    void registerCandidate_DuplicateEmail() {
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> authService.registerCandidate(registerRequest));

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should register recruiter successfully")
    void registerRecruiter_Success() {
        registerRequest.setCompanyName("TechCorp");
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });
        when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("test-token");

        AuthResponse response = authService.registerRecruiter(registerRequest);

        assertNotNull(response);
        assertEquals("RECRUITER", response.getRole());
        verify(recruiterProfileRepository).save(any());
    }

    @Test
    @DisplayName("Should login successfully")
    void login_Success() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "Password123");
        User user = new User("John", "Doe", "john@example.com", "encoded", Role.CANDIDATE);
        user.setId(1L);
        user.setActive(true);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("test-token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("test-token", response.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should throw BadCredentialsException on invalid login")
    void login_InvalidCredentials() {
        LoginRequest loginRequest = new LoginRequest("john@example.com", "wrong");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
