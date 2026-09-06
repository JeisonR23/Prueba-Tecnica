package com.pruebatecnica.profilecrud.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class AuthResourceTest {

    private String uniqueEmail() {
        return "test-" + UUID.randomUUID() + "@example.com";
    }

    private String profileJson(String email) {
        return """
                {
                  "name": "Jeison",
                  "lastName": "Reyes",
                  "cellphone": "8091234567",
                  "email": "%s",
                  "address": "San Francisco de Macoris"
                }
                """.formatted(email);
    }

    @Test
    void refreshToken_withValidToken_returnsNewDifferentToken() {
        Response created = given()
                .contentType(ContentType.JSON)
                .body(profileJson(uniqueEmail()))
                .when().post("/create-profile")
                .then().extract().response();

        String originalToken = created.path("token");

        given()
                .header("Authorization", "Bearer " + originalToken)
                .when().post("/refresh-token")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .body("token", not(originalToken));
    }

    @Test
    void refreshToken_withoutToken_returns401() {
        given()
                .when().post("/refresh-token")
                .then()
                .statusCode(401);
    }
}