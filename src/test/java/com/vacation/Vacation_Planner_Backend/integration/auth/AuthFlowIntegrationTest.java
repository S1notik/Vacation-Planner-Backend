package com.vacation.Vacation_Planner_Backend.integration.auth;

import com.vacation.Vacation_Planner_Backend.integration.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;


import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @ParameterizedTest
    @CsvSource({
            "employer@mail.ru, CEO, EMPLOYER",
            "employee@mail.ru, Worker, EMPLOYEE"
    })
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
    void protectedEndpoint_withoutToken_returns403() {
        given()
                .when()
                .get("/api/vacations/balance")
                .then()
                .statusCode(403);
    }

    @Test
    void protectedEndpoint_withInvalidToken_returns403() {
        given()
                .header("Authorization", "Bearer garbage.invalid.token")
                .when()
                .get("/api/vacations/balance")
                .then()
                .statusCode(403);
    }
}