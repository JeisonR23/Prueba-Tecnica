package com.pruebatecnica.profilecrud.entity;

import io.quarkus.mongodb.panache.PanacheMongoEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;


@MongoEntity(collection = "api_keys")
public class ApiKeyEntry extends PanacheMongoEntity {
    public String clientId;
    public String secretHash;
    public String description;
}
