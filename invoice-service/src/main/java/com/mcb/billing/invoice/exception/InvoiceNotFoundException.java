package com.mcb.billing.invoice.exception;

/** Thrown when no invoice exists for a given invoice number. Maps to HTTP 404. */
public class InvoiceNotFoundException extends RuntimeException {

    public InvoiceNotFoundException(String invoiceNumber) {
        super("No invoice found with number '" + invoiceNumber + "'");
    }
}
