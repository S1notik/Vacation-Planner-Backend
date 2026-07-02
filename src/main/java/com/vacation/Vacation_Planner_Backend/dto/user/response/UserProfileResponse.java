package com.vacation.Vacation_Planner_Backend.dto.user.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String email;
    private String name;
    private String role;
    private String phone;
    private String jobTitle;
    private String avatarUrl;
}
