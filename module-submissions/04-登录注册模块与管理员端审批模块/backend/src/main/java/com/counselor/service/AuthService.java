package com.counselor.service;

import com.counselor.dto.auth.LoginRequest;
import com.counselor.dto.auth.LoginResponse;
import com.counselor.dto.auth.RegisterRequest;

public interface AuthService {
    LoginResponse counselorLogin(LoginRequest request);
    LoginResponse adminLogin(LoginRequest request);
    void register(RegisterRequest request);
    java.util.Map<String,Object> registrationStatus(LoginRequest request);
    LoginResponse refresh(com.counselor.dto.auth.RefreshTokenRequest request);
}
