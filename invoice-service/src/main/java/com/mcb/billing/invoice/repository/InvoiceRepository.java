package com.mcb.billing.invoice.repository;

import com.mcb.billing.invoice.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Data-access layer for {@link Invoice}. CRUD comes from {@link JpaRepository};
 * the finder below is a Spring Data derived query.
 */
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    /** Look up an invoice by its business key. */
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);
}
