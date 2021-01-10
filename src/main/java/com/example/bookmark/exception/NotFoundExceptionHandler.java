package com.example.bookmark.exception;

import io.quarkus.qute.Template;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class NotFoundExceptionHandler implements ExceptionMapper<NotFoundException> {

    @Inject
    Template error;

    @Override
    public Response toResponse(NotFoundException exception) {
        String errorHtml = this.error.data("error", "The page doesn't exists!!").render();
        return Response.status(404).entity(errorHtml).build();
    }
    
}
