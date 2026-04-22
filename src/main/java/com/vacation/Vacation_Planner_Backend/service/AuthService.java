package com.vacation.Vacation_Planner_Backend.service;


import com.vacation.Vacation_Planner_Backend.dto.auth.request.LoginRequest;
import com.vacation.Vacation_Planner_Backend.dto.auth.request.RegisterRequest;
import com.vacation.Vacation_Planner_Backend.dto.auth.response.LoginResponse;
import com.vacation.Vacation_Planner_Backend.dto.auth.response.RegisterResponse;
import org.springframework.stereotype.Service;


public interface AuthService {
    // Register a new user and return tokens
    RegisterResponse register(RegisterRequest request);
    // Authenticate user and return tokens
    LoginResponse login(LoginRequest request);
}
