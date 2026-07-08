package com.mcb.billing.invoice.domain;

/**
 * Lifecycle status of an invoice. Stored as a string (see {@code @Enumerated(STRING)}
 * on the entity), so adding or reordering values later cannot corrupt existing data.
 *
 * <p>v1 only transitions DRAFT -> ISSUED; PAID and OVERDUE are reserved for later.
 */
public enum InvoiceStatus {
    DRAFT,
    ISSUED,
    PAID,
    OVERDUE
}
