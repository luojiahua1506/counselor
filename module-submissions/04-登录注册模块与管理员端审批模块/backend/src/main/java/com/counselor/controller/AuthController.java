package com.counselor.controller;

import com.counselor.dto.auth.LoginRequest;
import com.counselor.dto.auth.RegisterRequest;
import com.counselor.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/counselor/login")
    public ResponseEntity<?> counselorLogin(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.counselorLogin(request));
    }

    @PostMapping("/admin/login")
    public ResponseEntity<?> adminLogin(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.adminLogin(request));
    }

    @PostMapping("/counselor/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.ok(Map.of("message", "注册成功，请等待管理员审核"));
    }

    @PostMapping("/counselor/registration-status") public ResponseEntity<?> registrationStatus(@Valid @RequestBody LoginRequest request){return ResponseEntity.ok(authService.registrationStatus(request));}
    @PostMapping("/refresh") public ResponseEntity<?> refresh(@Valid @RequestBody com.counselor.dto.auth.RefreshTokenRequest request){return ResponseEntity.ok(authService.refresh(request));}
}
