package com.vacation.Vacation_Planner_Backend.dto.auth.request;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {
    @NotBlank(message = "Пользователь с таким email не найден")
    @Email(message = "Неккоректный email")
    private String email;

    @NotBlank(message = "Неверный пароль")
    @Size(min = 8, message = "Пароль минимум 8 символов")
    private String password;
}
