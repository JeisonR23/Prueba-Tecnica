package com.pruebatecnica.profilequery.resource;

import com.pruebatecnica.profilequery.client.ProfileCrudClient;
import com.pruebatecnica.profilequery.dto.ProfileResponse;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class ProfileQueryResource {

    @Inject
    JsonWebToken jwt;

    @Inject
    @RestClient
    ProfileCrudClient profileCrudClient;

    @GET
    @Path("/get-profile")
    @RolesAllowed("profile-owner")
    public ProfileResponse getProfile() {
        return profileCrudClient.getProfile(jwt.getSubject());
    }

    @GET
    @Path("/search-profile")
    @RolesAllowed("profile-owner")
    public ProfileResponse searchProfile(@QueryParam("email") String email) {
        ProfileResponse response = profileCrudClient.searchByEmail(email);
        if (!response.id.equals(jwt.getSubject())) {
            throw new NotFoundException("Profile not found");
        }
        return response;
    }
}