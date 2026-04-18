package com.vacation.Vacation_Planner_Backend.dto.team.request;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JoinTeamRequest {
    @NotBlank
    private String inviteCode;
}
