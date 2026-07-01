package com.vacation.Vacation_Planner_Backend.integration.vacation;

import com.vacation.Vacation_Planner_Backend.integration.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

public class VacationIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createVacation_withValidData_returns200() {
        String employerToken = register("vacemployer@mail.ru", "CEO", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employerToken, "vacTeam");
        String employeeToken = register("vacemployee@mail.ru", "Worker", "EMPLOYEE");
        inviteUser(employeeToken, inviteCode);
        String vacationId = createVacation(employeeToken, "2026-08-01", "2026-08-10");
        assertNotNull(vacationId);
    }

    @Test
    void employerCannotReviewVacationFromAnotherTeam() {
        // team B
        String employerB = register("isoEmployerB@mail.ru", "BossB", "EMPLOYER");
        String vacationIdB = createTeamWithVacation(employerB, "isoEmployeeB@mail.ru", "teamB");

        // team A
        String employerA = register("isoEmployerA@mail.ru", "BossA", "EMPLOYER");
        String response = given()
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
        assertTrue(response.contains("Access denied"));
    }

    @Test
    void employerCannotSetBalanceForEmployeeFromAnotherTeam() {
        // team B
        String employerB = register("balEmployerB@mail.ru", "BossB", "EMPLOYER");
        String inviteCodeB = createTeamAndGetInviteCode(employerB, "balTeamB");
        String employeeB = register("balEmployeeB@mail.ru", "WorkerB", "EMPLOYEE");
        inviteUser(employeeB, inviteCodeB);
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

    @Test
    void createVacation_whenNotInTeam_returns400() {
        String employee = register("notInTeamEmployee@mail.ru", "WorkerA", "EMPLOYEE");
        String errorBody = given()
                .header("Authorization", "Bearer " + employee)
                .contentType(ContentType.JSON)
                .body("""
                        {"startDate": "2026-08-01", "endDate": "2026-08-10"}
                        """)
                .when()
                .post("/api/vacations")
                .then()
                .statusCode(400)
                .extract().asString();
        assertTrue(errorBody.contains("User is not in a team"));
    }

    @Test
    void createVacation_withEndDateBeforeStartDate_returns400() {
        String employer = register("dateEmployer@mail.ru", "CEO", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employer, "dateTeam");
        String employee = register("dateEmployee@mail.ru", "Worker", "EMPLOYEE");
        inviteUser(employee, inviteCode);
        String response = given()
                .header("Authorization", "Bearer " + employee)
                .contentType(ContentType.JSON)
                .body("""
                        {"startDate": "2026-08-10", "endDate": "2026-08-01"}
                        """)
                .when()
                .post("/api/vacations")
                .then()
                .statusCode(400)
                .extract().asString();
        assertTrue(response.contains("End date cannot be before start date"));
    }

    @Test
    void reviewVacation_withInvalidStatus_returns400() {
        String employer = register("reviewEmployer@mail.ru", "CEO", "EMPLOYER");
        String vacationId = createTeamWithVacation(employer, "reviewStatusEmployee@mail.ru",
                "reviewTeam");
        String errorBody = given()
                .header("Authorization", "Bearer " + employer)
                .contentType(ContentType.JSON)
                .body("""
                        {"status": "APPLE"}
                        """)
                .when()
                .put("/api/vacations/{id}/review", vacationId)
                .then()
                .statusCode(400)
                .extract().asString();
        assertTrue(errorBody.contains("Invalid status"));
    }

    @Test
    void reviewVacation_whenAlreadyReviewed_returns400() {
        String employer = register("doubleReviewEmployer@mail.ru", "CEO", "EMPLOYER");
        String vacationId = createTeamWithVacation(employer, "doubleReviewEmployee@mail.ru",
                "doubleReviewTeam");
        given()
                .header("Authorization", "Bearer " + employer)
                .contentType(ContentType.JSON)
                .body("""
                        {"status": "APPROVED"}
                        """)
                .when()
                .put("/api/vacations/{id}/review", vacationId)
                .then()
                .statusCode(200);

        String errorBody = given()
                .header("Authorization", "Bearer " + employer)
                .contentType(ContentType.JSON)
                .body("""
                        {"status": "REJECTED"}
                        """)
                .when()
                .put("/api/vacations/{id}/review", vacationId)
                .then()
                .statusCode(400)
                .extract().asString();
        assertTrue(errorBody.contains("Vacation request is already reviewed"));
    }

    @Test
    void setTeamBalance_alsoUpdatesEmployerBalance() {
        String employer = register("teamBalEmployer@mail.ru", "CEO", "EMPLOYER");
        createTeamAndGetInviteCode(employer, "teamBalTeam");

        given()
                .header("Authorization", "Bearer " + employer)
                .queryParam("totalDays", 15)
                .when()
                .put("/api/vacations/balance/team")
                .then()
                .statusCode(200);

        int employerTotalDays = given()
                .header("Authorization", "Bearer " + employer)
                .when()
                .get("/api/teams/members")
                .then()
                .statusCode(200)
                .extract().path("find { it.role == 'EMPLOYER' }.totalDays");
        assertEquals(15, employerTotalDays);
    }

    @Test
    void usedDays_reflectApprovedVacation() {
        String employer = register("usedDaysEmployer@mail.ru", "CEO", "EMPLOYER");
        String vacationCode = createTeamWithVacation(employer, "usedDaysEmployee@mail.ru", "usedDaysTeam");
        given()
                .header("Authorization", "Bearer " + employer)
                .contentType(ContentType.JSON)
                .body("""
                        {"status": "APPROVED"}
                        """)
                .when()
                .put("/api/vacations/{id}/review", vacationCode)
                .then()
                .statusCode(200);
        int usedDays = given()
                .header("Authorization", "Bearer " + employer)
                .when()
                .get("/api/teams/members")
                .then()
                .statusCode(200)
                .extract().path("find { it.role == 'EMPLOYEE' }.usedDays");
        assertEquals(10, usedDays);
    }

    @Test
    void viewMyVacation_returns200() {
        String employer = register("userVacEmployer@mail.ru", "Geralt", "EMPLOYER");
        String vacationCode = createTeamAndGetInviteCode(employer, "userVacTeam");
        String employee = register("userVacEmployeer@mail.ru", "Jack", "EMPLOYEE");
        inviteUser(employee, vacationCode);
        createVacation(employee, "2026-08-01", "2026-08-10");
        given()
                .header("Authorization", "Bearer " + employee)
                .when()
                .get("/api/vacations/my")
                .then()
                .statusCode(200);
    }

    @Test
    void viewEmployeeBalance_returns200() {
        String employer = register("balanceEmployer@mail.ru", "Geralt", "EMPLOYER");
        String vacationCode = createTeamAndGetInviteCode(employer, "BalanceTeam");
        String employee = register("balanceEmployee@mail.ru", "Jack", "EMPLOYEE");
        inviteUser(employee, vacationCode);
        int totalDays = given()
                .header("Authorization", "Bearer " + employee)
                .when()
                .get("/api/vacations/balance")
                .then()
                .statusCode(200)
                .extract().path("totalDays");
        assertEquals(28, totalDays);
    }

    @Test
    void viewTeamVacations_asEmployee_returns403() {
        String employee = register("teamViewEmployee@mail.ru", "Worker", "EMPLOYEE");

        given()
                .header("Authorization", "Bearer " + employee)
                .when()
                .get("/api/vacations/team")
                .then()
                .statusCode(403);
    }

    @Test
    void viewTeamVacations_asEmployer_returns200() {
        String employer = register("teamViewHappyEmployer@mail.ru", "CEO", "EMPLOYER");
        createTeamWithVacation(employer, "teamViewHappyEmployee@mail.ru", "teamViewHappyTeam");
        String response = given()
                .header("Authorization", "Bearer " + employer)
                .when()
                .get("/api/vacations/team")
                .then()
                .statusCode(200)
                .extract().asString();
        assertTrue(response.contains("PENDING"));
    }

}