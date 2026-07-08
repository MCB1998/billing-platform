package com.mcb.billing.invoice.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * A single line on an invoice. Part of the Invoice aggregate: it has no independent
 * lifecycle and is always managed through its {@link Invoice}.
 *
 * <p>{@code lineTotal} (= quantity * unitPrice) is stored, not recomputed on read,
 * because an invoice is a legal document whose amounts are frozen.
 */
@Entity
@Table(name = "invoice_items")
public class InvoiceItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The owning invoice (foreign key {@code invoice_id} lives on this table). */
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false)
    private Invoice invoice;

    @NotBlank
    @Size(max = 300)
    @Column(nullable = false, length = 300)
    private String description;

    @Positive
    @Column(nullable = false)
    private int quantity;

    @NotNull
    @PositiveOrZero
    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @NotNull
    @PositiveOrZero
    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    /** No-args constructor required by JPA/Hibernate. */
    protected InvoiceItem() {
    }

    /** Creates a line and computes its total (quantity * unitPrice). */
    public InvoiceItem(String description, int quantity, BigDecimal unitPrice) {
        this.description = description;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.lineTotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    /** Package-private: set by {@link Invoice#addItem(InvoiceItem)} to sync both sides. */
    void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Long getId() {
        return id;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public BigDecimal getLineTotal() {
        return lineTotal;
    }
}
