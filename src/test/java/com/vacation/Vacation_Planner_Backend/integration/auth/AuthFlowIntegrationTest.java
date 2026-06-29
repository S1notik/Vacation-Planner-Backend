package com.vacation.Vacation_Planner_Backend.integration.auth;

import com.vacation.Vacation_Planner_Backend.integration.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Test
    void registerEmloyer_withValidData_returns200AndToken() {
        String body = """
                {"email": "employer@mail.ru", "password": "password123", "name": "CEO", "role": "EMPLOYER"}
                """;

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
}