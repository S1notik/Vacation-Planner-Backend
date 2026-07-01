package com.vacation.Vacation_Planner_Backend.integration.team;

import com.vacation.Vacation_Planner_Backend.integration.AbstractIntegrationTest;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TeamIntegrationTest extends AbstractIntegrationTest {

    @Test
    void createTeam_withValidData_returns200() {
        String token = register("createteamuser@mail.ru", "CEO", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(token, "levTeam");
        assertNotNull(inviteCode);
    }

    @Test
    void joinTeam_withValidInviteCode_returns200() {
        String employerToken = register("joinemployer@mail.ru", "CEO", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employerToken, "levTeam");

        // create employee for join team by invite code
        String employeeToken = register("joinemployee@mail.ru", "Jack", "EMPLOYEE");
        String joinResponse = given()
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
        assertTrue(joinResponse.contains("levTeam"));
    }

    @Test
    void joinTeam_withInvalidInviteCode_returns400() {
        String wrongInviteCode = "wrongCode";

        // create employee for join team by invite code
        String employeeToken = register("wrongemployee@mail.ru", "Jack", "EMPLOYEE");
        String joinResponse = given()
                .header("Authorization", "Bearer " + employeeToken)
                .contentType(ContentType.JSON)
                .body("""
                        {"inviteCode": "%s"}
                        """.formatted(wrongInviteCode))
                .when()
                .post("/api/teams/join")
                .then()
                .statusCode(400)
                .extract().asString();
        assertTrue(joinResponse.contains("Invalid invite code"));
    }

    @Test
    void viewTeamMembers_returns200() {
        String employer = register(uniqueEmail(), "BOB", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employer, "membersTeam");
        String employee = register(uniqueEmail(), "Jack", "EMPLOYEE");
        inviteUser(employee, inviteCode);
        String response = given()
                .header("Authorization", "Bearer " + employer)
                .when()
                .get("/api/teams/members")
                .then()
                .statusCode(200)
                .extract().asString();
        assertTrue(response.contains("Jack"));
    }
    @Test
    void viewTeamCalendar_returns200() {
        String employer = register(uniqueEmail(), "BOB", "EMPLOYER");
        String vacationId = createTeamWithVacation(employer, uniqueEmail(), "calendarTeam");
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
        String response = given()
                .header("Authorization", "Bearer " + employer)
                .when()
                .get("/api/teams/calendar")
                .then()
                .statusCode(200)
                .extract().asString();
        assertTrue(response.contains("APPROVED"));
    }

    @Test
    void joinTeam_whenAlreadyMemberOfAnyTeam_returns409() {
        String employer = register(uniqueEmail(), "BOB", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employer, "membersTeam");
        String employee = register(uniqueEmail(), "Jack", "EMPLOYEE");
        inviteUser(employee, inviteCode);
        String errorBody = given()
                .header("Authorization", "Bearer " + employee)
                .contentType(ContentType.JSON)
                .body("""
                        {"inviteCode": "%s"}
                        """.formatted(inviteCode))
                .when()
                .post("/api/teams/join")
                .then()
                .statusCode(409)
                .extract().asString();
        assertTrue(errorBody.contains("You are already a member of a team"));
    }

    @Test
    void createTeam_whenEmployerAlreadyHaveTeam_returns409() {
        String employer = register(uniqueEmail(), "BOB", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employer, "membersTeam");
        String body = """
                {"name": "%s"}
                """.formatted("membersTeam2");
        String errorBody = given()
                .header("Authorization", "Bearer " + employer)
                .contentType(ContentType.JSON)
                .body(body)
                .when()
                .post("/api/teams")
                .then()
                .statusCode(409)
                .extract().asString();
        assertTrue(errorBody.contains("You already have a team"));
    }
}
