package com.mcb.billing.invoice.web;

import com.mcb.billing.invoice.service.InvoiceService;
import com.mcb.billing.invoice.web.dto.CreateInvoiceRequestDTO;
import com.mcb.billing.invoice.web.dto.InvoiceResponseDTO;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * REST endpoints for invoices. Invoices are addressed by their business key
 * {@code invoiceNumber}.
 */
@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService service;

    public InvoiceController(InvoiceService service) {
        this.service = service;
    }

    /** Creates a DRAFT invoice (validates the customer via the customer-service). */
    @PostMapping
    public ResponseEntity<InvoiceResponseDTO> create(@Valid @RequestBody CreateInvoiceRequestDTO request) {
        InvoiceResponseDTO created = service.createInvoice(request);
        URI location = URI.create("/invoices/" + created.invoiceNumber());
        return ResponseEntity.created(location).body(created);
    }

    /** Issues an invoice (DRAFT -> ISSUED). Modeled as an action sub-resource. */
    @PostMapping("/{invoiceNumber}/issue")
    public InvoiceResponseDTO issue(@PathVariable String invoiceNumber) {
        return service.issueInvoice(invoiceNumber);
    }

    @GetMapping("/{invoiceNumber}")
    public InvoiceResponseDTO getOne(@PathVariable String invoiceNumber) {
        return service.getByInvoiceNumber(invoiceNumber);
    }

    @GetMapping
    public List<InvoiceResponseDTO> listAll() {
        return service.listInvoices();
    }
}
