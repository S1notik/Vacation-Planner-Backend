package com.vacation.Vacation_Planner_Backend.dto.vacation.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VacationResponse {
    private UUID id;
    private LocalDateTime start_date;
    private LocalDateTime end_date;
    private int daysCount;
    private String status;
    private String comment;
    private String createdAt;
}