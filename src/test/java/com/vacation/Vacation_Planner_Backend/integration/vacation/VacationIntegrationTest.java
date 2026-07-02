package com.vacation.Vacation_Planner_Backend.integration.vacation;

import com.vacation.Vacation_Planner_Backend.integration.AbstractIntegrationTest;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Vacation Planner API")
@Feature("Vacation Management")
public class VacationIntegrationTest extends AbstractIntegrationTest {

    @Test
    @Story("Создание отпуска")
    @DisplayName("Создание отпуска с валидными данными, возвращает 200")
    @Severity(SeverityLevel.CRITICAL)
    void createVacation_withValidData_returns200() {
        String employerToken = register("vacemployer@mail.ru", "CEO", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employerToken, "vacTeam");
        String employeeToken = register("vacemployee@mail.ru", "Worker", "EMPLOYEE");
        inviteUser(employeeToken, inviteCode);
        String vacationId = createVacation(employeeToken, "2026-08-01", "2026-08-10");
        assertNotNull(vacationId);
    }

    @Test
    @Story("Изоляция данных между командами")
    @DisplayName("Работодатель не может ревьюить заявку чужой команды, возвращает 403")
    @Severity(SeverityLevel.BLOCKER)
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
    @Story("Изоляция данных между командами")
    @DisplayName("Работодатель не может менять баланс сотрудника чужой команды, возвращает403")
    @Severity(SeverityLevel.BLOCKER)
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
    @Story("Создание отпуска")
    @DisplayName("Создание отпуска вне команды, возвращает 400")
    @Severity(SeverityLevel.NORMAL)
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
    @Story("Создание отпуска")
    @DisplayName("Создание отпуска с датой конца раньше начала, возвращает 400")
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
    @Story("Ревью отпуска")
    @DisplayName("Ревью с невалидным статусом, возвращает 400")
    @Severity(SeverityLevel.NORMAL)
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
    @Story("Ревью отпуска")
    @DisplayName("Повторное ревью заявки, возвращает 400")
    @Severity(SeverityLevel.NORMAL)
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
    @Story("Баланс отпусков")
    @DisplayName("Установка баланса команды включает работодателя")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Баланс отпусков")
    @DisplayName("usedDays отражает одобренный отпуск")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Просмотр отпусков")
    @DisplayName("Сотрудник видит свои заявки на отпуск")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Баланс отпусков")
    @DisplayName("Сотрудник видит свой баланс (28 дней по умолчанию)")
    @Severity(SeverityLevel.CRITICAL)
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
    @Story("Просмотр отпусков")
    @DisplayName("Сотрудник не может смотреть отпуска команды, возвращает 403")
    @Severity(SeverityLevel.NORMAL)
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
    @Story("Просмотр отпусков")
    @DisplayName("Работодатель видит заявки своей команды")
    @Severity(SeverityLevel.CRITICAL)
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

    @Test
    @Story("Создание отпуска")
    @DisplayName("Создание отпуска при нехватке дней, возвращает 400")
    @Severity(SeverityLevel.NORMAL)
    void createVacation_whenNotEnoughDays_returns400() {
        String employer = register(uniqueEmail(), "CEO", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employer, "notEnoughTeam");
        String employee = register(uniqueEmail(), "Worker", "EMPLOYEE");
        inviteUser(employee, inviteCode);
        String response = given()
                .header("Authorization", "Bearer " + employee)
                .contentType(ContentType.JSON)
                .body("""
                        {"startDate": "2026-01-01", "endDate": "2026-12-31"}
                        """)
                .when()
                .post("/api/vacations")
                .then()
                .statusCode(400)
                .extract().asString();
        assertTrue(response.contains("Not enough vacation days"));
    }

    @Test
    @Story("Отмена отпуска")
    @DisplayName("Отмена своей заявки, возвращает 200")
    void deleteVacation_ownRequest_returns200() {
        String employer = register(uniqueEmail(), "CEO", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employer, "delTeam");
        String employee = register(uniqueEmail(), "Worker", "EMPLOYEE");
        inviteUser(employee, inviteCode);
        String vacationId = createVacation(employee, "2026-08-01", "2026-08-10");
        given()
                .header("Authorization", "Bearer " + employee)
                .when()
                .delete("/api/vacations/{id}", vacationId)
                .then()
                .statusCode(200);
    }

    @Test
    @Story("Отмена отпуска")
    @DisplayName("Нельзя отменить уже одобренную заявку, возвращает 400")
    @Severity(SeverityLevel.NORMAL)
    void deleteVacation_alreadyApproved_returns400() {
        String employer = register(uniqueEmail(), "Bob", "EMPLOYER");
        String inviteCode = createTeamAndGetInviteCode(employer, "delTeam");
        String employee = register(uniqueEmail(), "Worker", "EMPLOYEE");
        inviteUser(employee, inviteCode);
        String vacationId = createVacation(employee, "2026-08-01", "2026-08-10");
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
                .header("Authorization", "Bearer " + employee)
                .when()
                .delete("/api/vacations/{id}", vacationId)
                .then()
                .statusCode(400)
                .extract().asString();
        assertTrue(errorBody.contains("Only pending vacation requests can be cancelled"));
    }

    @Test
    @Story("Ревью отпуска")
    @DisplayName("Одобрение заявки возвращает 200 и статус APPROVED")
    void reviewVacation_approve_returns200() {
        String employer = register(uniqueEmail(), "CEO", "EMPLOYER");
        String vacationId = createTeamWithVacation(employer, uniqueEmail(), "reviewApproveTeam");

        given()
                .header("Authorization", "Bearer " + employer)
                .contentType(ContentType.JSON)
                .body("""
                        {"status": "APPROVED"}
                        """)
                .when()
                .put("/api/vacations/{id}/review", vacationId)
                .then()
                .statusCode(200)
                .body("status", org.hamcrest.Matchers.equalTo("APPROVED"));
    }
}