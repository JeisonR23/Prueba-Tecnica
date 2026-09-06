package com.pruebatecnica.profilecrud.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateProfileRequest {

    @NotBlank(message = "Name is required")
    public String name;

    @NotBlank(message = "Last Name is required")
    public String lastName;

    @NotBlank(message = "Cellphone is required")
    public String cellphone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    public String email;

    @NotBlank(message = "Address is required")
    public String address;

}
