package com.vacation.Vacation_Planner_Backend.integration;

import com.vacation.Vacation_Planner_Backend.security.TokenBlacklistService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import static io.restassured.RestAssured.given;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:18");

    static {
        postgres.start();
    }

    @LocalServerPort
    protected int port;

    @MockitoBean
    protected TokenBlacklistService tokenBlacklistService;

    @BeforeEach
    void setUpRestAssured() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        RestAssured.replaceFiltersWith(new io.qameta.allure.restassured.AllureRestAssured());
    }

    protected String register(String email, String name, String role) {
        String body = """
                {"email": "%s", "password": "password123", "name": "%s", "role": "%s"}
                """.formatted(email, name, role);
        return given()
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/auth/register")
                .then()
                .statusCode(200)
                .extract().path("accessToken");
    }

    protected String createTeamAndGetInviteCode(String employerToken, String teamName) {
        String body = """
                {"name": "%s"}
                """.formatted(teamName);
        return given()
                .header("Authorization", "Bearer " + employerToken)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/teams")
                .then()
                .statusCode(200)
                .extract().path("inviteCode");
    }

    protected String inviteUser(String employeeToken, String inviteCode) {
        return given()
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(ContentType.JSON)
                .body("""
                        {"inviteCode": "%s"}
                        """.formatted(inviteCode))
                .when()
                .post("/api/teams/join")
                .then()
                .statusCode(200)
                .extract().asString();
    }

    protected String createVacation(String employeeToken, String startDate, String endDate) {
        return given()
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(ContentType.JSON)
                .body("""
                        {"startDate": "%s", "endDate": "%s"}
                        """.formatted(startDate, endDate))
                .when().post("/api/vacations")
                .then().statusCode(200).extract().path("id");
    }

    protected String createTeamWithVacation(String employerToken, String employeeEmail, String teamName) {
        String inviteCode = createTeamAndGetInviteCode(employerToken, teamName);
        String employeeToken = register(employeeEmail, "Worker", "EMPLOYEE");
        inviteUser(employeeToken, inviteCode);
        return createVacation(employeeToken, "2026-08-01", "2026-08-10");
    }

    protected String uniqueEmail() {
        return "user-" + java.util.UUID.randomUUID() + "@mail.ru";
    }
}