package com.example.bookmark.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class ResponseStatusExceptionHandler implements ExceptionMapper<ResponseStatusException> {

    @Override
    public Response toResponse(ResponseStatusException e) {
        return Response.status(e.getStatus()).entity(e.getMessage()).build();
    }
}
