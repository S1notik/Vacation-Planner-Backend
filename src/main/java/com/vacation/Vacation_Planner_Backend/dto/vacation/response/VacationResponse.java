package com.vacation.Vacation_Planner_Backend.dto.vacation.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacationResponse {
    private UUID id;
    private LocalDate startDate;
    private LocalDate endDate;
    private int daysCount;
    private String status;
    private String comment;
    private String createdAt;
}