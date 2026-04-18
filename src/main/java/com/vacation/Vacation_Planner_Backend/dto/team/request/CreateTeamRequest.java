package com.vacation.Vacation_Planner_Backend.dto.team.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTeamRequest {
    @NotBlank(message = "Название команды обязательно")
    @Size(min = 2, max = 255, message = "Название от 2 до 255 символов")
    private String name;
}
