package com.campus.lostfound.exception;

/**
 * Thrown when an operation violates domain business rules (e.g. invalid status transitions, claiming own item).
 */
public class BusinessRuleException extends RuntimeException {

    public BusinessRuleException(String message) {
        super(message);
    }
}

