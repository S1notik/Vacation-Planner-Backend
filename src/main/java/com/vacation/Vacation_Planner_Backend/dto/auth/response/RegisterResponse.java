package com.vacation.Vacation_Planner_Backend.dto.auth.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterResponse {
    private String accessToken;
    private String refreshToken;
    private String role;  // so that Android knows which screen to show
}
