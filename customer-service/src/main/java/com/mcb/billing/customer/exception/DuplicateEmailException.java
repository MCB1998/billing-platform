package com.mcb.billing.customer.exception;

/**
 * Thrown when trying to create a customer with an email that already exists.
 * A {@code @RestControllerAdvice} will later translate this into an HTTP 409.
 */
public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException(String email) {
        super("A customer with email '" + email + "' already exists");
    }
}
