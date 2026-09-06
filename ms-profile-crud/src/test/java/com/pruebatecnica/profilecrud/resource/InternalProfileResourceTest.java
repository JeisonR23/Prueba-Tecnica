package com.pruebatecnica.profilecrud.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;

@QuarkusTest
class InternalProfileResourceTest {

    @ConfigProperty(name = "internal.client.id")
    String clientId;

    @ConfigProperty(name = "internal.client.secret")
    String clientSecret;

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
    void getInternalProfile_withoutAuth_returns401() {
        given()
                .when().get("/internal/profile/000000000000000000000000")
                .then()
                .statusCode(401);
    }

    @Test
    void getInternalProfile_withWrongCredentials_returns401() {
        given()
                .auth().preemptive().basic(clientId, "clave-incorrecta")
                .when().get("/internal/profile/000000000000000000000000")
                .then()
                .statusCode(401);
    }

    @Test
    void getInternalProfile_withValidPair_returns200() {
        String email = uniqueEmail();
        String id = given()
                .contentType(ContentType.JSON)
                .body(profileJson(email))
                .when().post("/create-profile")
                .then().extract().path("profile.id");

        given()
                .auth().preemptive().basic(clientId, clientSecret)
                .when().get("/internal/profile/" + id)
                .then()
                .statusCode(200)
                .body("email", equalTo(email));
    }

    @Test
    void searchByEmail_withValidPair_returns200() {
        String email = uniqueEmail();
        given()
                .contentType(ContentType.JSON)
                .body(profileJson(email))
                .when().post("/create-profile")
                .then().statusCode(201);

        given()
                .auth().preemptive().basic(clientId, clientSecret)
                .queryParam("email", email)
                .when().get("/internal/profile/search")
                .then()
                .statusCode(200)
                .body("email", equalTo(email));
    }

    @Test
    void searchByEmail_withUnknownEmail_returns404() {
        given()
                .auth().preemptive().basic(clientId, clientSecret)
                .queryParam("email", "no-existe-" + UUID.randomUUID() + "@example.com")
                .when().get("/internal/profile/search")
                .then()
                .statusCode(404);
    }
}