package com.mcb.billing.customer.service;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;

/**
 * Generates the next unique customer number (e.g. "C-00001") from a database
 * sequence. Using a DB sequence (instead of, say, count()+1 or an in-memory
 * counter) makes generation safe under concurrency and across multiple service
 * instances, because the database is the single point of coordination.
 */
@Component
public class CustomerNumberGenerator {

    private final EntityManager entityManager;

    CustomerNumberGenerator(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public String next() {
        Number value = (Number) entityManager
                .createNativeQuery("select nextval('customer_number_seq')")
                .getSingleResult();
        return "C-%05d".formatted(value.longValue());
    }
}
