package com.pruebatecnica.profilecrud.dto;

public class ProfileCreatedResponse {

    public ProfileResponse profile;
     public String token;

    public ProfileCreatedResponse(ProfileResponse profile, String token) {
        this.profile = profile;
        this.token = token;
    }

    
}
