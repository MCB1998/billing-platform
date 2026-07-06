package com.mcb.billing.customer.web;

import com.mcb.billing.customer.service.CustomerService;
import com.mcb.billing.customer.web.dto.CreateCustomerRequestDTO;
import com.mcb.billing.customer.web.dto.CustomerResponseDTO;
import com.mcb.billing.customer.web.dto.UpdateCustomerRequestDTO;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

/**
 * REST endpoints for customer master data. Internal/back-office API: callers are
 * admins and other services, not the customers themselves. Customers are addressed
 * by their business key {@code customerNumber}, never by the internal database id.
 */
@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    /** Creates a customer. Returns 201 with a Location header pointing to the new resource. */
    @PostMapping
    public ResponseEntity<CustomerResponseDTO> create(@Valid @RequestBody CreateCustomerRequestDTO request) {
        CustomerResponseDTO created = service.createCustomer(request);
        URI location = URI.create("/customers/" + created.customerNumber());
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{customerNumber}")
    public CustomerResponseDTO getOne(@PathVariable String customerNumber) {
        return service.getByCustomerNumber(customerNumber);
    }

    /** All customers, including deactivated ones. */
    @GetMapping
    public List<CustomerResponseDTO> listAll() {
        return service.listAllCustomers();
    }

    /** Only active (not soft-deleted) customers. */
    @GetMapping("/active")
    public List<CustomerResponseDTO> listActive() {
        return service.listActiveCustomers();
    }

    @PutMapping("/{customerNumber}")
    public CustomerResponseDTO update(@PathVariable String customerNumber,
                                      @Valid @RequestBody UpdateCustomerRequestDTO request) {
        return service.updateCustomer(customerNumber, request);
    }

    /** Soft-deletes the customer (sets it inactive). Returns 204 No Content. */
    @DeleteMapping("/{customerNumber}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deactivate(@PathVariable String customerNumber) {
        service.deactivateCustomer(customerNumber);
    }
}
