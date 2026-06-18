package com.appverse.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when a resource already exists and a duplicate would be created.
 * Maps to HTTP 409 Conflict.
 *
 * Examples: duplicate email on registration, user reviewing same app twice.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
