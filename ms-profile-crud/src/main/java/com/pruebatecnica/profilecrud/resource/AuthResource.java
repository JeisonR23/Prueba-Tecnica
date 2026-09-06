package com.pruebatecnica.profilecrud.resource;

import com.pruebatecnica.profilecrud.dto.TokenResponse;
import com.pruebatecnica.profilecrud.service.JwtService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class AuthResource {

    @Inject
    JwtService jwtService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/refresh-token")
    @RolesAllowed("profile-owner")
    public TokenResponse refreshToken() {
        String token = jwtService.issueToken(jwt.getSubject());
        return new TokenResponse(token);
    }
}