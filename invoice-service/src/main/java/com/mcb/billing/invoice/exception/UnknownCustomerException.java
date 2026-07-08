package com.mcb.billing.invoice.exception;

/**
 * Thrown when an invoice references a customer that does not exist in the
 * customer-service (Feign 404). Maps to HTTP 422.
 */
public class UnknownCustomerException extends RuntimeException {

    public UnknownCustomerException(String customerNumber) {
        super("Customer '" + customerNumber + "' does not exist");
    }
}
