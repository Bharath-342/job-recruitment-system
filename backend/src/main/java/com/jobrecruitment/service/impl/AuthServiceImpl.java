package com.jobrecruitment.service.impl;

import com.jobrecruitment.dto.request.LoginRequest;
import com.jobrecruitment.dto.request.RegisterRequest;
import com.jobrecruitment.dto.response.AuthResponse;
import com.jobrecruitment.entity.*;
import com.jobrecruitment.exception.BusinessRuleException;
import com.jobrecruitment.exception.DuplicateResourceException;
import com.jobrecruitment.repository.*;
import com.jobrecruitment.security.JwtTokenProvider;
import com.jobrecruitment.service.AuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final CandidateProfileRepository candidateProfileRepository;
    private final RecruiterProfileRepository recruiterProfileRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    public AuthServiceImpl(UserRepository userRepository,
                           CandidateProfileRepository candidateProfileRepository,
                           RecruiterProfileRepository recruiterProfileRepository,
                           PasswordEncoder passwordEncoder,
                           JwtTokenProvider jwtTokenProvider,
                           AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.candidateProfileRepository = candidateProfileRepository;
        this.recruiterProfileRepository = recruiterProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public AuthResponse registerCandidate(RegisterRequest request) {
        validateEmailNotExists(request.getEmail());

        User user = createUser(request, Role.CANDIDATE);
        userRepository.save(user);

        // Create empty candidate profile
        CandidateProfile profile = new CandidateProfile();
        profile.setUser(user);
        candidateProfileRepository.save(profile);

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return buildAuthResponse(token, user);
    }

    @Override
    @Transactional
    public AuthResponse registerRecruiter(RegisterRequest request) {
        validateEmailNotExists(request.getEmail());

        if (request.getCompanyName() == null || request.getCompanyName().isBlank()) {
            throw new BusinessRuleException("Company name is required for recruiter registration");
        }

        User user = createUser(request, Role.RECRUITER);
        userRepository.save(user);

        // Create recruiter profile
        RecruiterProfile profile = new RecruiterProfile();
        profile.setUser(user);
        profile.setCompanyName(request.getCompanyName());
        recruiterProfileRepository.save(profile);

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return buildAuthResponse(token, user);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BusinessRuleException("Your account has been deactivated. Please contact admin.");
        }

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return buildAuthResponse(token, user);
    }

    private void validateEmailNotExists(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("An account with this email already exists");
        }
    }

    private User createUser(RegisterRequest request, Role role) {
        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().toLowerCase().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setActive(true);
        return user;
    }

    private AuthResponse buildAuthResponse(String token, User user) {
        return new AuthResponse(token, user.getEmail(), user.getFirstName(),
                user.getLastName(), user.getRole().name(), user.getId());
    }
}
