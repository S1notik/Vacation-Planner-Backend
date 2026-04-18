package com.vacation.Vacation_Planner_Backend.dto.team.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinTeamResponse {
    private UUID teamId;
    private String teamName;
    private String employerName;
    private String joinedAt;
}
