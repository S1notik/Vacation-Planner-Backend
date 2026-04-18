package com.vacation.Vacation_Planner_Backend.dto.auth.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import java.util.UUID;


@Data
@AllArgsConstructor
@NoArgsConstructor
class RegisterRequest {
    @NotBlank(message = "Email обязателен")
    @Email(message = "Неккоректный email")
    private String email;

    @NotBlank(message = "Пароль обязателен")
    @Size(min = 8, message = "Пароль минимум 8 символов")
    private String password;

    @NotBlank(message = "Имя обязательно")
    private String name;

    @NotBlank(message = "Роль обязательна")
    private String role;
}
