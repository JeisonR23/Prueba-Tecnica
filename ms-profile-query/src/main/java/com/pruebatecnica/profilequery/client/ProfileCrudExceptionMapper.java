package com.pruebatecnica.profilequery.client;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.ext.ResponseExceptionMapper;

/**
 * Por defecto, el cliente REST de Quarkus envuelve CUALQUIER respuesta que
 * no sea 2xx en un WebApplicationException generico - no distingue un 404
 * de un 500. Sin este mapper, @CircuitBreaker/@Fallback con
 * skipOn=NotFoundException.class nunca matchea, y un simple "perfil no
 * existe" se trataria como si ms-profile-crud estuviera caido.
 */
public class ProfileCrudExceptionMapper implements ResponseExceptionMapper<WebApplicationException> {

    @Override
    public WebApplicationException toThrowable(Response response) {
        if (response.getStatus() == 404) {
            return new NotFoundException(response);
        }
        return new WebApplicationException(response);
    }
}