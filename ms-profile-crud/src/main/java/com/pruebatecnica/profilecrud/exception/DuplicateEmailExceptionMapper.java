package com.pruebatecnica.profilecrud.exception;

import com.mongodb.DuplicateKeyException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DuplicateEmailExceptionMapper implements ExceptionMapper<DuplicateKeyException> {

    @Override
    public Response toResponse(DuplicateKeyException exception) {
        return Response.status(Response.Status.CONFLICT)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorBody("Ya existe un perfil con ese email"))
                .build();
    }

    public record ErrorBody(String error) {
    }
}