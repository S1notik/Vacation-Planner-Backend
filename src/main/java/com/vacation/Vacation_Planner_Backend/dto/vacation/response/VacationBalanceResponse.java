package com.vacation.Vacation_Planner_Backend.dto.vacation.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacationBalanceResponse {
    private int totalDays;
    private int usedDays;
    private int remainingDays;  // total - used
    private int year;
}
