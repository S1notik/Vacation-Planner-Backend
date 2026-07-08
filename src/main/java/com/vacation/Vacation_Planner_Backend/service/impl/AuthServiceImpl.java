package com.vacation.Vacation_Planner_Backend.service.impl;

import com.vacation.Vacation_Planner_Backend.dto.auth.request.LoginRequest;
import com.vacation.Vacation_Planner_Backend.dto.auth.request.RegisterRequest;
import com.vacation.Vacation_Planner_Backend.dto.auth.response.LoginResponse;
import com.vacation.Vacation_Planner_Backend.dto.auth.response.RegisterResponse;
import com.vacation.Vacation_Planner_Backend.exception.BadRequestException;
import com.vacation.Vacation_Planner_Backend.exception.ConflictException;
import com.vacation.Vacation_Planner_Backend.exception.NotFoundException;
import com.vacation.Vacation_Planner_Backend.model.entity.User;
import com.vacation.Vacation_Planner_Backend.model.enums.Role;
import com.vacation.Vacation_Planner_Backend.repository.UserRepository;
import com.vacation.Vacation_Planner_Backend.security.JwtService;
import com.vacation.Vacation_Planner_Backend.security.TokenBlacklistService;
import com.vacation.Vacation_Planner_Backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Override
    public RegisterResponse register(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ConflictException("Email already in use");
        }
        User user = User.builder()
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(Role.valueOf(request.getRole()))
                .jobTitle(request.getJobTitle())
                .build();
        userRepository.save(user);
        // Generate tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new RegisterResponse(accessToken, refreshToken, user.getRole().name());
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        // Verify email and password
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        // Load user from database
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        return new LoginResponse(accessToken, refreshToken, user.getRole().name(), user.getName());
    }

    // Logout — add token to blacklist
    @Override
    public void logout(String token) {
        tokenBlacklistService.blacklistToken(token, jwtExpiration);
    }

    @Override
    public LoginResponse refresh(String refreshToken) {
        // Extract email from refresh token
        String email = jwtService.extractEmail(refreshToken);
        // Load user from database
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        // Validate refresh token
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new BadRequestException("Invalid refresh token");
        }
        // Generate new access token
        String newAccessToken = jwtService.generateToken(user);
        return new LoginResponse(newAccessToken, refreshToken, user.getRole().name(), user.getName());
    }
}