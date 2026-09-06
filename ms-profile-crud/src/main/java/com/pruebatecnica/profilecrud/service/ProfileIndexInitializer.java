package com.pruebatecnica.profilecrud.service;

import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.pruebatecnica.profilecrud.entity.Person;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class ProfileIndexInitializer {

    void onStart(@Observes StartupEvent event) {
        Person.mongoCollection().createIndex(
                Indexes.ascending("email"),
                new IndexOptions().unique(true)
        );
    }
}