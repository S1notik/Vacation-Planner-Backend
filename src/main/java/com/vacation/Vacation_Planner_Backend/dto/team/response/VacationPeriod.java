package com.vacation.Vacation_Planner_Backend.dto.team.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacationPeriod {
    private LocalDate startDate;
    private LocalDate endDate;
    private int daysCount;
    private String status;
}