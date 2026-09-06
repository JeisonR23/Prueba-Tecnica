package com.pruebatecnica.profilecrud.mapper;

import com.pruebatecnica.profilecrud.dto.PatchProfileRequest;
import com.pruebatecnica.profilecrud.dto.ProfileResponse;
import com.pruebatecnica.profilecrud.entity.Person;

public class ProfileMapper {

    private ProfileMapper() {
    }

    public static ProfileResponse toResponse(Person person) {
        return new ProfileResponse(
                person.id.toHexString(),
                person.name,
                person.lastName,
                person.cellphone,
                person.email,
                person.address
        );
    }

    public static void applyRequest(Person person, String name, String lastName, String cellphone, String email, String address) {
        person.name = name;
        person.lastName = lastName;
        person.cellphone = cellphone;
        person.email = email;
        person.address = address;
    }

    public static void applyPatch(Person person, PatchProfileRequest request) {
        if (request.name != null) person.name = request.name;
        if (request.lastName != null) person.lastName = request.lastName;
        if (request.cellphone != null) person.cellphone = request.cellphone;
        if (request.email != null) person.email = request.email;
        if (request.address != null) person.address = request.address;
    }
}