package com.mcb.billing.invoice.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Generates the next unique invoice number (e.g. "INV-00001") from a database
 * sequence. Same approach as the customer-service: the database sequence is the
 * single point of coordination, so numbering is safe under concurrency and across
 * multiple service instances.
 */
@Component
public class InvoiceNumberGenerator {

    private final EntityManager entityManager;

    InvoiceNumberGenerator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String next() {
        Number value = (Number) entityManager
                .createNativeQuery("select nextval('invoice_number_seq')")
                .getSingleResult();
        return "INV-%05d".formatted(value.longValue());
    }
}
