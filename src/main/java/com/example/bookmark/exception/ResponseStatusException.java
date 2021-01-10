package com.example.bookmark.exception;

import lombok.Getter;

import javax.ws.rs.core.Response.Status;

@Getter
public class ResponseStatusException extends RuntimeException {

    private final Status status;

    public ResponseStatusException(String message, Status status) {
        super(message);
        this.status = status;
    }
}
