package com.pruebatecnica.profilequery.client;

import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.client.ClientRequestContext;
import jakarta.ws.rs.client.ClientRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Le agrega el header de Basic Auth (el par de llaves) a toda llamada
 * que este cliente le haga a ms-profile-crud.
 */
@ApplicationScoped
@Priority(Priorities.AUTHENTICATION)
public class BasicAuthClientFilter implements ClientRequestFilter {

    @ConfigProperty(name = "internal.client.id")
    String clientId;

    @ConfigProperty(name = "internal.client.secret")
    String clientSecret;

    @Override
    public void filter(ClientRequestContext requestContext) throws IOException {
        String credentials = clientId + ":" + clientSecret;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes(StandardCharsets.UTF_8));
        requestContext.getHeaders().putSingle(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
    }
}