package com.vacation.Vacation_Planner_Backend.dto.team.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTeamResponse {
    private UUID id;
    private String name;
    private String inviteCode;
    private String inviteQrUrl;
    private String createdAt;
}
