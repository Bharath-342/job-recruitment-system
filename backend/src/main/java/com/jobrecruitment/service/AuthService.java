package com.jobrecruitment.service;

import com.jobrecruitment.dto.request.LoginRequest;
import com.jobrecruitment.dto.request.RegisterRequest;
import com.jobrecruitment.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse registerCandidate(RegisterRequest request);
    AuthResponse registerRecruiter(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}
