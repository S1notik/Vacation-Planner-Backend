package com.vacation.Vacation_Planner_Backend.integration.vacation;

import com.vacation.Vacation_Planner_Backend.integration.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.C;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VacationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createVacation_withValidData_returns200() {
        String employerToken = register("vacemployer@mail.ru", "CEO", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employerToken, "vacTeam");

        String employeeToken = register("vacemployee@mail.ru", "Worker", "EMPLOYEE");
        given()
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(ContentType.JSON)
                .body("""
                        {"inviteCode": "%s"}
                        """.formatted(inviteCode))
                .when()
                .post("/api/teams/join")
                .then()
                .statusCode(200);

        String vacationId = given()
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(ContentType.JSON)
                .body("""
                        {"startDate": "2026-08-01", "endDate": "2026-08-10"}
                        """)
                .when()
                .post("/api/vacations")
                .then()
                .statusCode(200)
                .extract().path("id");
        assertNotNull(vacationId);
    }

    @Test
    void employerCannotReviewVacationFromAnotherTeam() {
        // team B
        String employerB = register("isoEmployerB@mail.ru", "BossB", "EMPLOYER");
        String inviteCodeB = createTeamAndGetInviteCode(employerB, "teamB");
        String employeeB = register("isoEmployeeB@mail.ru", "WorkerB", "EMPLOYEE");
        given()
                .header("Authorization", "Bearer " + employeeB)
                .contentType(ContentType.JSON)
                .body("""
                        {"inviteCode": "%s"}
                        """.formatted(inviteCodeB))
                .when()
                .post("/api/teams/join")
                .then()
                .statusCode(200);

        String vacationIdB = given()
                .header("Authorization", "Bearer " + employeeB)
                .contentType(ContentType.JSON)
                .body("""
                        {"startDate": "2026-08-01", "endDate": "2026-08-10"}
                        """)
                .when()
                .post("/api/vacations")
                .then()
                .statusCode(200)
                .extract().path("id");

        // team A
        String employerA = register("isoEmployerA@mail.ru", "BossA", "EMPLOYER");

        String vacationResponse = given()
                .header("Authorization", "Bearer " + employerA)
                .contentType(ContentType.JSON)
                .body("""
                        {"status": "APPROVED"}
                        """)
                .when()
                .put("/api/vacations/{id}/review", vacationIdB)
                .then()
                .statusCode(403)
                .extract().asString();
        assertTrue(vacationResponse.contains("Access denied"));
    }

    @Test
    void employerCannotSetBalanceForEmployeeFromAnotherTeam() {
        // team B
        String employerB = register("balEmployerB@mail.ru", "BossB", "EMPLOYER");
        String inviteCodeB = createTeamAndGetInviteCode(employerB, "balTeamB");

        String employeeB = register("balEmployeeB@mail.ru", "WorkerB", "EMPLOYEE");
        given()
                .header("Authorization", "Bearer " + employeeB)
                .contentType(ContentType.JSON)
                .body("""
                        {"inviteCode": "%s"}
                        """.formatted(inviteCodeB))
                .when()
                .post("/api/teams/join")
                .then()
                .statusCode(200);
        String employeeId = given()
                .header("Authorization", "Bearer " + employeeB)
                .contentType(ContentType.JSON)
                .when()
                .get("/api/teams/members")
                .then()
                .statusCode(200)
                .extract().path("find { it.role == 'EMPLOYEE' }.id");

        // team A
        String employerA = register("balEmployerA@mail.ru", "BossA", "EMPLOYER");
        createTeamAndGetInviteCode(employerA, "teamA");

        String vacationResponse = given()
                .header("Authorization", "Bearer " + employerA)
                .queryParam("totalDays", 20)
                .when()
                .put("/api/vacations/balance/{employeeId}", employeeId)
                .then()
                .statusCode(403)
                .extract().asString();
        assertTrue(vacationResponse.contains("Access denied"));
    }

}