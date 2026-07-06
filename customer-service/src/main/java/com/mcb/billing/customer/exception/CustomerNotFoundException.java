package com.mcb.billing.customer.exception;

/**
 * Thrown when no customer exists for a given customer number.
 * A {@code @RestControllerAdvice} will later translate this into an HTTP 404.
 */
public class CustomerNotFoundException extends RuntimeException {

    public CustomerNotFoundException(String customerNumber) {
        super("No customer found with number '" + customerNumber + "'");
    }
}
