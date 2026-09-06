package com.pruebatecnica.profilecrud.dto;

import jakarta.validation.constraints.Email;

public class PatchProfileRequest {

    public String name;
    public String lastName;
    public String cellphone;

    @Email(message = "email must be valid")
    public String email;

    public String address;
}