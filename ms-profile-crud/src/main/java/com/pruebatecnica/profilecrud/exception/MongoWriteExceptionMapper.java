package com.pruebatecnica.profilecrud.exception;

import com.mongodb.MongoWriteException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class MongoWriteExceptionMapper implements ExceptionMapper<MongoWriteException> {

    private static final int DUPLICATE_KEY_ERROR_CODE = 11000;

    @Override
    public Response toResponse(MongoWriteException exception) {
        if (exception.getError().getCode() == DUPLICATE_KEY_ERROR_CODE) {
            return Response.status(Response.Status.CONFLICT)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorBody("Ya existe un perfil con ese email", 409))
                    .build();
        }
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorBody("Error guardando el perfil", 500))
                .build();
    }

    public record ErrorBody(String error, int status) {
    }
}