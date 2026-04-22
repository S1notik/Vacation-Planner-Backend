package com.vacation.Vacation_Planner_Backend.controller;

import com.vacation.Vacation_Planner_Backend.dto.auth.request.LoginRequest;
import com.vacation.Vacation_Planner_Backend.dto.auth.request.RegisterRequest;
import com.vacation.Vacation_Planner_Backend.dto.auth.response.LoginResponse;
import com.vacation.Vacation_Planner_Backend.dto.auth.response.RegisterResponse;
import com.vacation.Vacation_Planner_Backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    // Register a new user
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    // Authenticate existing user
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    // Logout user
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader) {
        // Extract token from "Bearer <token>"
        String token = authHeader.substring(7);
        authService.logout(token);
        return ResponseEntity.ok().build();
    }

    // Refresh access token
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @RequestHeader("Authorization") String authHeader) {
        String refreshToken = authHeader.substring(7);
        return ResponseEntity.ok(authService.refresh(refreshToken));
    }
}