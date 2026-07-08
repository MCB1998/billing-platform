package com.mcb.billing.invoice.service;

import com.mcb.billing.invoice.client.CustomerClient;
import com.mcb.billing.invoice.client.CustomerDTO;
import com.mcb.billing.invoice.domain.Invoice;
import com.mcb.billing.invoice.exception.CustomerServiceUnavailableException;
import com.mcb.billing.invoice.exception.InactiveCustomerException;
import com.mcb.billing.invoice.exception.InvoiceNotFoundException;
import com.mcb.billing.invoice.exception.UnknownCustomerException;
import com.mcb.billing.invoice.mapper.InvoiceMapper;
import com.mcb.billing.invoice.repository.InvoiceRepository;
import com.mcb.billing.invoice.web.dto.CreateInvoiceRequestDTO;
import com.mcb.billing.invoice.web.dto.InvoiceResponseDTO;
import feign.FeignException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for invoices. Validates the referenced customer against the
 * customer-service (via Feign), builds the invoice aggregate and persists it.
 */
@Service
public class InvoiceService {

    private final InvoiceRepository repository;
    private final InvoiceMapper mapper;
    private final InvoiceNumberGenerator numberGenerator;
    private final CustomerClient customerClient;

    public InvoiceService(InvoiceRepository repository,
                          InvoiceMapper mapper,
                          InvoiceNumberGenerator numberGenerator,
                          CustomerClient customerClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.numberGenerator = numberGenerator;
        this.customerClient = customerClient;
    }

    /**
     * Creates a DRAFT invoice: validates the customer exists and is active (via
     * Feign), builds the aggregate, assigns a generated invoice number and saves.
     */
    @Transactional
    public InvoiceResponseDTO createInvoice(CreateInvoiceRequestDTO request) {
        CustomerDTO customer = fetchCustomer(request.customerNumber());
        if (!customer.active()) {
            throw new InactiveCustomerException(request.customerNumber());
        }

        Invoice invoice = mapper.toEntity(request);
        invoice.setInvoiceNumber(numberGenerator.next());
        Invoice saved = repository.save(invoice);
        return mapper.toResponse(saved);
    }

    /** Transitions a DRAFT invoice to ISSUED (dirty checking flushes the change). */
    @Transactional
    public InvoiceResponseDTO issueInvoice(String invoiceNumber) {
        Invoice invoice = requireInvoice(invoiceNumber);
        invoice.issue();
        return mapper.toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public InvoiceResponseDTO getByInvoiceNumber(String invoiceNumber) {
        return mapper.toResponse(requireInvoice(invoiceNumber));
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> listInvoices() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    /** Calls the customer-service and translates Feign errors into domain errors. */
    private CustomerDTO fetchCustomer(String customerNumber) {
        try {
            return customerClient.getCustomer(customerNumber);
        } catch (FeignException.NotFound e) {
            throw new UnknownCustomerException(customerNumber);
        } catch (FeignException e) {
            throw new CustomerServiceUnavailableException("customer-service is currently unavailable");
        }
    }

    private Invoice requireInvoice(String invoiceNumber) {
        return repository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new InvoiceNotFoundException(invoiceNumber));
    }
}
