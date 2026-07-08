package com.mcb.billing.invoice.exception;

/**
 * Thrown when an operation is not allowed in the invoice's current status
 * (e.g. issuing an invoice that is not a DRAFT). Maps to HTTP 409.
 */
public class InvalidInvoiceStateException extends RuntimeException {

    public InvalidInvoiceStateException(String message) {
        super(message);
    }
}
