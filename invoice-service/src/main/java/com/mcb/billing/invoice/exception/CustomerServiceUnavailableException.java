package com.mcb.billing.invoice.exception;

/**
 * Thrown when the customer-service cannot be reached or fails (any Feign error
 * other than 404). Maps to HTTP 503.
 */
public class CustomerServiceUnavailableException extends RuntimeException {

    public CustomerServiceUnavailableException(String message) {
        super(message);
    }
}
