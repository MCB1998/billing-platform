package com.mcb.billing.invoice.mapper;

import com.mcb.billing.invoice.domain.Invoice;
import com.mcb.billing.invoice.domain.InvoiceItem;
import com.mcb.billing.invoice.web.dto.CreateInvoiceItemDTO;
import com.mcb.billing.invoice.web.dto.CreateInvoiceRequestDTO;
import com.mcb.billing.invoice.web.dto.InvoiceItemResponseDTO;
import com.mcb.billing.invoice.web.dto.InvoiceResponseDTO;
import org.springframework.stereotype.Component;

import java.util.List;

/** Translates between the Invoice aggregate and its DTOs. */
@Component
public class InvoiceMapper {

    private static final String DEFAULT_CURRENCY = "EUR";

    /**
     * Builds a new DRAFT invoice aggregate from the request. Adding each item via
     * {@code addItem} keeps the total in sync, so totalAmount is already correct.
     * The invoiceNumber is assigned by the service, not here.
     */
    public Invoice toEntity(CreateInvoiceRequestDTO dto) {
        String currency = (dto.currency() == null || dto.currency().isBlank())
                ? DEFAULT_CURRENCY : dto.currency();

        Invoice invoice = new Invoice(dto.customerNumber(), currency, dto.dueDate());
        for (CreateInvoiceItemDTO item : dto.items()) {
            invoice.addItem(new InvoiceItem(item.description(), item.quantity(), item.unitPrice()));
        }
        return invoice;
    }

    public InvoiceResponseDTO toResponse(Invoice invoice) {
        List<InvoiceItemResponseDTO> items = invoice.getItems().stream()
                .map(item -> new InvoiceItemResponseDTO(
                        item.getDescription(), item.getQuantity(),
                        item.getUnitPrice(), item.getLineTotal()))
                .toList();

        return new InvoiceResponseDTO(
                invoice.getInvoiceNumber(),
                invoice.getCustomerNumber(),
                invoice.getStatus(),
                invoice.getCurrency(),
                invoice.getTotalAmount(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                items,
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }
}
