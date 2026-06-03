package com.vacation.Vacation_Planner_Backend.dto.team.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamMemberResponse {
    private UUID id;
    private String name;
    private String email;
    private String role;
    private String joinedAt;
    private int totalDays;
    private int usedDays;
}