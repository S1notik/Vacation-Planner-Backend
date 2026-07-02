package com.vacation.Vacation_Planner_Backend.dto.user.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    private String name;
    private String phone;
    private String jobTitle;
    private String avatarUrl;
}
