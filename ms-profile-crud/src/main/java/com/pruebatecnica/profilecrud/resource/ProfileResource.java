package com.pruebatecnica.profilecrud.resource;

import com.pruebatecnica.profilecrud.dto.CreateProfileRequest;
import com.pruebatecnica.profilecrud.dto.PatchProfileRequest;
import com.pruebatecnica.profilecrud.dto.ProfileCreatedResponse;
import com.pruebatecnica.profilecrud.dto.UpdateProfileRequest;
import com.pruebatecnica.profilecrud.entity.Person;
import com.pruebatecnica.profilecrud.mapper.ProfileMapper;
import com.pruebatecnica.profilecrud.service.JwtService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProfileResource {

    @Inject
    JwtService jwtService;

    @Inject
    JsonWebToken jwt;

    @POST
    @Path("/create-profile")
    public Response createProfile(@Valid CreateProfileRequest request) {
        Person person = new Person();
        ProfileMapper.applyRequest(person, request.name, request.lastName, request.cellphone, request.email, request.address);
        person.persist();

        String token = jwtService.issueToken(person.id.toHexString());
        ProfileCreatedResponse body = new ProfileCreatedResponse(ProfileMapper.toResponse(person), token);
        return Response.status(Response.Status.CREATED).entity(body).build();
    }

    @PUT
    @Path("/update-profile/{id}")
    @RolesAllowed("profile-owner")
    public Response updateProfile(@PathParam("id") String id, @Valid UpdateProfileRequest request) {
        requireOwnership(id);
        Person person = findOrNotFound(id);
        ProfileMapper.applyRequest(person, request.name, request.lastName, request.cellphone, request.email, request.address);
        person.update();
        return Response.ok(ProfileMapper.toResponse(person)).build();
    }

    @PATCH
    @Path("/update-profile/{id}")
    @RolesAllowed("profile-owner")
    public Response patchProfile(@PathParam("id") String id, @Valid PatchProfileRequest request) {
        requireOwnership(id);
        Person person = findOrNotFound(id);
        ProfileMapper.applyPatch(person, request);
        person.update();
        return Response.ok(ProfileMapper.toResponse(person)).build();
    }

    @DELETE
    @Path("/delete-profile/{id}")
    @RolesAllowed("profile-owner")
    public Response deleteProfile(@PathParam("id") String id) {
        requireOwnership(id);
        Person person = findOrNotFound(id);
        person.delete();
        return Response.noContent().build();
    }

    private void requireOwnership(String id) {
        if (!id.equals(jwt.getSubject())) {
            throw new ForbiddenException("Token does not belong to this profile");
        }
    }

    private Person findOrNotFound(String id) {
        Person person;
        try {
            person = Person.findById(new ObjectId(id));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Profile not found");
        }
        if (person == null) {
            throw new NotFoundException("Profile not found");
        }
        return person;
    }
}