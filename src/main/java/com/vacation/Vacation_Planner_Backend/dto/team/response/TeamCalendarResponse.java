package com.vacation.Vacation_Planner_Backend.dto.team.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamCalendarResponse {
    private UUID employeeId;
    private String employeeName;
    private List<VacationPeriod> vacations;
}