package com.campus.lostfound.exception;

/**
 * Thrown when an entity violates uniqueness constraints (e.g. duplicate email, duplicate claim).
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}

