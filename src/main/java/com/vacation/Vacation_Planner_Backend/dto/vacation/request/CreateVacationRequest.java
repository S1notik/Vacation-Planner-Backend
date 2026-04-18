package com.vacation.Vacation_Planner_Backend.dto.vacation.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateVacationRequest {
    @NotNull
    private LocalDate startDate;
    @NotNull
    private LocalDate endDate;
    private String comment;
}
