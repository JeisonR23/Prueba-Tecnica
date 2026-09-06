package com.pruebatecnica.profilecrud.security;

import com.pruebatecnica.profilecrud.service.ApiKeyService;
import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * Valida que la llamada entrante traiga Basic Auth con un par
 * clientId/secret que exista y coincida (ApiKeyService la busca y la
 * compara con bcrypt). Esta es la comunicacion segura entre
 * ms-profile-query y ms-profile-crud.
 */
@InternalAuth
@Provider
@Priority(Priorities.AUTHENTICATION)
public class InternalAuthFilter implements ContainerRequestFilter {

    @Inject
    ApiKeyService apiKeyService;

    @Override
    public void filter(ContainerRequestContext requestContext) {
        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.regionMatches(true, 0, "Basic ", 0, 6)) {
            reject(requestContext, "Missing Basic Auth credentials");
            return;
        }

        String decoded;
        try {
            decoded = new String(Base64.getDecoder().decode(authHeader.substring(6).trim()), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            reject(requestContext, "Malformed Basic Auth header");
            return;
        }

        int separator = decoded.indexOf(':');
        if (separator < 0) {
            reject(requestContext, "Malformed Basic Auth header");
            return;
        }

        String clientId = decoded.substring(0, separator);
        String secret = decoded.substring(separator + 1);

        if (!apiKeyService.isValidPair(clientId, secret)) {
            reject(requestContext, "Client id and secret are not a valid pair");
        }
    }

    private void reject(ContainerRequestContext requestContext, String message) {
        requestContext.abortWith(
                Response.status(Response.Status.UNAUTHORIZED)
                        .entity(new ErrorResponse(message))
                        .build()
        );
    }

    public record ErrorResponse(String error) {
    }
}