package com.pruebatecnica.profilecrud.resource;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@QuarkusTest
class ProfileResourceTest {

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
    void createProfile_returnsTokenAndProfile() {
        String email = uniqueEmail();
        given()
                .contentType(ContentType.JSON)
                .body(profileJson(email))
                .when().post("/create-profile")
                .then()
                .statusCode(201)
                .body("token", notNullValue())
                .body("profile.id", notNullValue())
                .body("profile.email", equalTo(email));
    }

    @Test
    void createProfile_missingEmail_returns400() {
        given()
                .contentType(ContentType.JSON)
                .body("""
                        {
                          "name": "Jeison",
                          "lastName": "Reyes",
                          "cellphone": "8091234567",
                          "address": "San Francisco de Macoris"
                        }
                        """)
                .when().post("/create-profile")
                .then()
                .statusCode(400);
    }

    @Test
    void createProfile_duplicateEmail_returns409() {
        String email = uniqueEmail();
        given()
                .contentType(ContentType.JSON)
                .body(profileJson(email))
                .when().post("/create-profile")
                .then()
                .statusCode(201);

        given()
                .contentType(ContentType.JSON)
                .body(profileJson(email))
                .when().post("/create-profile")
                .then()
                .statusCode(409);
    }

    @Test
    void updateProfile_withoutToken_returns401() {
        given()
                .contentType(ContentType.JSON)
                .body(profileJson(uniqueEmail()))
                .when().put("/update-profile/000000000000000000000000")
                .then()
                .statusCode(401);
    }

    @Test
    void updateProfile_withSomeoneElsesToken_returns403() {
        String tokenA = given()
                .contentType(ContentType.JSON)
                .body(profileJson(uniqueEmail()))
                .when().post("/create-profile")
                .then().extract().path("token");

        String idB = given()
                .contentType(ContentType.JSON)
                .body(profileJson(uniqueEmail()))
                .when().post("/create-profile")
                .then().extract().path("profile.id");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + tokenA)
                .body(profileJson(uniqueEmail()))
                .when().put("/update-profile/" + idB)
                .then()
                .statusCode(403);
    }

    @Test
    void updateProfile_withOwnToken_returns200() {
        Response created = given()
                .contentType(ContentType.JSON)
                .body(profileJson(uniqueEmail()))
                .when().post("/create-profile")
                .then().extract().response();

        String token = created.path("token");
        String id = created.path("profile.id");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body(profileJson(uniqueEmail()).replace("Reyes", "Reyes Actualizado"))
                .when().put("/update-profile/" + id)
                .then()
                .statusCode(200)
                .body("lastName", equalTo("Reyes Actualizado"));
    }

    @Test
    void patchProfile_withOwnToken_updatesOnlyGivenField() {
        Response created = given()
                .contentType(ContentType.JSON)
                .body(profileJson(uniqueEmail()))
                .when().post("/create-profile")
                .then().extract().response();

        String token = created.path("token");
        String id = created.path("profile.id");
        String originalEmail = created.path("profile.email");

        given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + token)
                .body("""
                        {
                          "address": "Direccion parcial nueva"
                        }
                        """)
                .when().patch("/update-profile/" + id)
                .then()
                .statusCode(200)
                .body("address", equalTo("Direccion parcial nueva"))
                .body("email", equalTo(originalEmail));
    }

    @Test
    void deleteProfile_withOwnToken_returns204() {
        Response created = given()
                .contentType(ContentType.JSON)
                .body(profileJson(uniqueEmail()))
                .when().post("/create-profile")
                .then().extract().response();

        String token = created.path("token");
        String id = created.path("profile.id");

        given()
                .header("Authorization", "Bearer " + token)
                .when().delete("/delete-profile/" + id)
                .then()
                .statusCode(204);
    }
}