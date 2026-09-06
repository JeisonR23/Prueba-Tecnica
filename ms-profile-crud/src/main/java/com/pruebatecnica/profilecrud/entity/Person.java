package com.pruebatecnica.profilecrud.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;

@MongoEntity(collection = "profiles")
public class Person extends PanacheMongoEntity {
    public String name;
    public String lastName;
    public String cellphone;
    public String email;
    public String address;
}
