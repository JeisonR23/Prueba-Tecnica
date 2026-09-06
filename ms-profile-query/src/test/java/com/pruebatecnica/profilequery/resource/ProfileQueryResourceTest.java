package com.pruebatecnica.profilequery.resource;

import com.pruebatecnica.profilequery.client.ProfileCrudClient;
import com.pruebatecnica.profilequery.dto.ProfileResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.InjectMock;
import io.quarkus.test.security.TestSecurity;
import io.quarkus.test.security.jwt.Claim;
import io.quarkus.test.security.jwt.JwtSecurity;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServiceUnavailableException;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * ms-profile-query no tiene la llave privada, asi que no puede firmar un JWT
 * real para probar. En vez de eso, @TestSecurity + @JwtSecurity le meten la
 * identidad directo al contexto de seguridad del test, sin pasar por la
 * verificacion real del token. El ProfileCrudClient se mockea con @InjectMock
 * porque estos tests no dependen de que ms-profile-crud este corriendo.
 */
@QuarkusTest
class ProfileQueryResourceTest {

    private static final String PROFILE_ID = "000000000000000000000001";

    @InjectMock
    @RestClient
    ProfileCrudClient profileCrudClient;

    @Test
    void getProfile_withoutToken_returns401() {
        given()
                .when().get("/get-profile")
                .then()
                .statusCode(401);
    }

    @Test
    @TestSecurity(user = PROFILE_ID, roles = "profile-owner")
    @JwtSecurity(claims = { @Claim(key = "sub", value = PROFILE_ID) })
    void getProfile_withValidToken_returnsProfileFromCrud() {
        ProfileResponse mocked = new ProfileResponse(
                PROFILE_ID, "Jeison", "Perez", "8091234567", "jeison@example.com", "San Francisco de Macoris");
        when(profileCrudClient.getProfile(eq(PROFILE_ID))).thenReturn(mocked);

        given()
                .when().get("/get-profile")
                .then()
                .statusCode(200)
                .body("email", equalTo("jeison@example.com"));
    }

    @Test
    @TestSecurity(user = PROFILE_ID, roles = "profile-owner")
    @JwtSecurity(claims = { @Claim(key = "sub", value = PROFILE_ID) })
    void getProfile_whenCrudSaysNotFound_returns404() {
        when(profileCrudClient.getProfile(eq(PROFILE_ID))).thenThrow(new NotFoundException());

        given()
                .when().get("/get-profile")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = PROFILE_ID, roles = "profile-owner")
    @JwtSecurity(claims = { @Claim(key = "sub", value = PROFILE_ID) })
    void getProfile_whenCrudUnavailable_returns503() {
        when(profileCrudClient.getProfile(eq(PROFILE_ID))).thenThrow(new ServiceUnavailableException());

        given()
                .when().get("/get-profile")
                .then()
                .statusCode(503);
    }
}