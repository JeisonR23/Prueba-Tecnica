package com.pruebatecnica.profilecrud.dto;

public class ProfileResponse {
    public String id;
    public String name;
    public String lastName;
   public String cellphone;
    public String email;
    public String address;

    public ProfileResponse(){
    }

    public ProfileResponse(String id, String name, String lastName, String cellphone, String email, String address) {
        this.id = id;
        this.name = name;
        this.lastName = lastName;
        this.cellphone = cellphone;
        this.email = email;
        this.address = address;
    }
}
