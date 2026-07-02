package com.vacation.Vacation_Planner_Backend.integration.user;

import com.vacation.Vacation_Planner_Backend.integration.AbstractIntegrationTest;
import io.qameta.allure.*;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@Epic("Vacation Planner API")
@Feature("User Profile")
public class UserProfileIntegrationTest extends AbstractIntegrationTest {

    @Test
    @Story("Просмотр профиля")
    @DisplayName("GET /me возвращает профиль текущего пользователя")
    @Severity(SeverityLevel.NORMAL)
    void getMe_returnsCurrentUserProfile() {
        String email = uniqueEmail();
        String token = register(email, "Arina", "EMPLOYER");
        String response = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(200)
                .extract().asString();
        assertTrue(response.contains(email));
    }

    @Test
    @Story("Редактирование профиля")
    @DisplayName("PATCH /me сохраняет обновлённые данные профиля")
    @Severity(SeverityLevel.NORMAL)
    void updateMe_returnsUpdateUserProfile() {
        String email = uniqueEmail();
        String token = register(email, "Syoma", "EMPLOYER");
        String response = given()
                .header("Authorization", "Bearer " + token)
                .contentType(ContentType.JSON)
                .body("""
                        {"phone": "+79001234567", "jobTitle": "Senior Developer"}
                        """)
                .when()
                .patch("/api/users/me")
                .then()
                .statusCode(200)
                .extract().asString();
        assertTrue(response.contains("+79001234567"));
        assertTrue(response.contains("Senior Developer"));

        String getResponse = given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/users/me")
                .then()
                .statusCode(200)
                .extract().asString();
        assertTrue(getResponse.contains("+79001234567"));
    }

}
