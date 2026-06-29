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
}
