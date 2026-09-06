package com.pruebatecnica.profilecrud.resource;

import com.pruebatecnica.profilecrud.dto.ProfileResponse;
import com.pruebatecnica.profilecrud.entity.Person;
import com.pruebatecnica.profilecrud.mapper.ProfileMapper;
import com.pruebatecnica.profilecrud.security.InternalAuth;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import org.bson.types.ObjectId;

@Path("/internal/profile")
@Produces(MediaType.APPLICATION_JSON)
@InternalAuth
public class InternalProfileResource {

    @GET
    @Path("/{id}")
    public ProfileResponse getProfile(@PathParam("id") String id) {
        Person person;
        try {
            person = Person.findById(new ObjectId(id));
        } catch (IllegalArgumentException e) {
            throw new NotFoundException("Profile not found");
        }
        if (person == null) {
            throw new NotFoundException("Profile not found");
        }
        return ProfileMapper.toResponse(person);
    }

    @GET
    @Path("/search")
    public ProfileResponse searchByEmail(@QueryParam("email") String email) {
        Person person = (Person) Person.find("email", email).firstResultOptional().orElse(null);
        if (person == null) {
            throw new NotFoundException("Profile not found");
        }
        return ProfileMapper.toResponse(person);
    }
}