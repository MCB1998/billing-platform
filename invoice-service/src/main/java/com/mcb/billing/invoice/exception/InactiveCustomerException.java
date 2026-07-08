package com.mcb.billing.invoice.exception;

/**
 * Thrown when trying to invoice a customer that is inactive (soft-deleted).
 * Maps to HTTP 422.
 */
public class InactiveCustomerException extends RuntimeException {

    public InactiveCustomerException(String customerNumber) {
        super("Customer '" + customerNumber + "' is inactive and cannot be invoiced");
    }
}
