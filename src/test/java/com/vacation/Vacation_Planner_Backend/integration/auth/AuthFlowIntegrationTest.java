package com.vacation.Vacation_Planner_Backend.integration.auth;

import com.vacation.Vacation_Planner_Backend.integration.AbstractIntegrationTest;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Epic("Vacation Planner API")
@Feature("Authentication")
public class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @CsvSource({
            "employer@mail.ru, CEO, EMPLOYER",
            "employee@mail.ru, Worker, EMPLOYEE"
    })
    @Story("Регистрация пользователя")
    @DisplayName("Регистрация с валидными данными, возвращает токен")
    @Severity(SeverityLevel.CRITICAL)
    void register_withValidData_returns200AndToken(String email, String name, String role) {
        String body = """
                {"email": "%s", "password": "password123", "name": "%s", "role": "%s"}
                """.formatted(email, name, role);

        String token = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .extract().path("accessToken");

        assertNotNull(token);
    }


    @Test
    @Story("Регистрация пользователя")
    @DisplayName("Регистрация работодателя уже с существующим email, возвращает 409")
    @Severity(SeverityLevel.NORMAL)
    void registerEmployer_withDuplicateEmail_returns409() {
        register("dup@mail.ru", "Worker", "EMPLOYER");

        String body = """
                {"email": "dup@mail.ru", "password": "password123", "name": "CEO", "role": "EMPLOYER"}
                """;
        String responseBody = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().
                post("/api/auth/register")
                .then()
                .statusCode(409)
                .extract().asString();
        assertTrue(responseBody.contains("Email already in use"));
    }

    @Test
    @Story("Регистрация пользователя")
    @DisplayName("Регистрация сотрудника уже с существующим email, возвращает 409")
    @Severity(SeverityLevel.NORMAL)
    void registerEmployee_withDuplicateEmail_returns409() {
        register("dupemp@mail.ru", "Worker", "EMPLOYEE");

        String body = """
                {"email": "dupemp@mail.ru", "password": "password123", "name": "Worker", "role": "EMPLOYEE"}
                """;
        String responseBody = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when().
                post("/api/auth/register")
                .then()
                .statusCode(409)
                .extract().asString();
        assertTrue(responseBody.contains("Email already in use"));
    }

    @Test
    @Story("Вход пользователя")
    @DisplayName("Вход работодателя с валидными данными, возвращает токен")
    @Severity(SeverityLevel.CRITICAL)
    void loginEmployer_withValidData_returns200AndToken() {
        register("loginemployer@mail.ru", "Worker", "EMPLOYER");

        String body = """
                {"email": "loginemployer@mail.ru", "password": "password123"}
                """;
        String token = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().path("accessToken");
        assertNotNull(token);
    }

    @Test
    @Story("Вход пользователя")
    @DisplayName("Вход сотрудника с валидными данными, возвращает токен")
    @Severity(SeverityLevel.CRITICAL)
    void loginEmployee_withValidData_returns200AndToken() {
        register("loginemployee@mail.ru", "Worker", "EMPLOYEE");

        String body = """
                {"email": "loginemployee@mail.ru", "password": "password123"}
                """;
        String token = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(200)
                .extract().path("accessToken");
        assertNotNull(token);
    }

    @Test
    @Story("Вход пользователя")
    @DisplayName("Вход пользователя с несуществующим email, возвращает 401")
    @Severity(SeverityLevel.CRITICAL)
    void login_withNonexistentEmail_returns401() {
        String body = """
                {"email": "ghost@mail.ru", "password": "password123"}
                """;

        String responseBody = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .extract().asString();
        assertTrue(responseBody.contains("Unauthorized"));
    }

    @Test
    @Story("Вход пользователя")
    @DisplayName("Вход пользователя с неправильным паролем, возвращает 401")
    @Severity(SeverityLevel.CRITICAL)
    void login_withWrongPassword_returns401() {
        String body = """
                {"email": "real@mail.ru", "password": "wrongpassword"}
                """;
        register("real@mail.ru", "Vlad", "EMPLOYEE");

        String responseBody = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/login")
                .then()
                .statusCode(401)
                .extract().asString();
        assertTrue(responseBody.contains("Unauthorized"));
    }

    @Test
    @Story("Вход пользователя")
    @DisplayName("Регистрация пользователя со слабым паролем, возвращает 400")
    @Severity(SeverityLevel.NORMAL)
    void register_withWeakPassword_return400() {
        String body = """
                {"email": "weakpassword@mail.ru", "password": "123", "name": "Karina", "role": "EMPLOYER"}
                """;
        String errorBody = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(400)
                .extract().asString();
        assertTrue(errorBody.contains("Пароль минимум 8 символов"));
    }

    @Test
    @Story("Обновление токена")
    @DisplayName("Обновление токена по refresh-токену, возвращает новый access-токен")
    @Severity(SeverityLevel.CRITICAL)
    void refresh_withValidToken_returns200() {
        String refreshToken = given()
                .contentType(ContentType.JSON)
                .body("""
                        {"email": "%s", "password": "password123", "name": "R", "role": "EMPLOYEE"}
                        """.formatted(uniqueEmail()))
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .extract().path("refreshToken");

        String newAccessToken = given()
                .header("Authorization", "Bearer " + refreshToken)
                .when()
                .post("/api/auth/refresh")
                .then()
                .statusCode(200)
                .extract().path("accessToken");
        assertNotNull(newAccessToken);
    }

    @Test
    @Story("Защита эндпоинтов")
    @DisplayName("Запрос без токена, возвращает 403")
    @Severity(SeverityLevel.BLOCKER)
    void protectedEndpoint_withoutToken_returns403() {
        given()
                .when()
                .get("/api/vacations/balance")
                .then()
                .statusCode(403);
    }

    @Test
    @Story("Защита эндпоинтов")
    @DisplayName("Запрос с битым токеном, возвращает 403")
    @Severity(SeverityLevel.BLOCKER)
    void protectedEndpoint_withInvalidToken_returns403() {
        given()
                .header("Authorization", "Bearer garbage.invalid.token")
                .when()
                .get("/api/vacations/balance")
                .then()
                .statusCode(403);
    }

    @Test
    @Story("Регистрация пользователя")
    @DisplayName("Должность, указанная при регистрации, доступна в профиле")
    @Severity(SeverityLevel.NORMAL)
    void register_withJobTitle_savesit() {
        String email = uniqueEmail();
        String body = """
                {"email": "%s", "password": "password123", "name": "Jack", "role": "EMPLOYER",
                 "jobTitle": "CEO"}
                """.formatted(email);
        String token = given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .extract().path("accessToken");
        String jobTitle = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(200)
                .extract().path("jobTitle");
        assertEquals("CEO", jobTitle);
    }
}