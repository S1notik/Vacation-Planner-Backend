package com.vacation.Vacation_Planner_Backend.dto.vacation.request;


import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReviewVacationRequest {
    @NotNull
    private String status;
    private String reason;
}
