package com.campus.lostfound.exception;

/**
 * Thrown when a requested resource (User, ItemReport, Claim) cannot be found by its identifier.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}

