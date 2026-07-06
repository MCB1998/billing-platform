package com.mcb.billing.customer.service;

import com.mcb.billing.customer.domain.Customer;
import com.mcb.billing.customer.exception.CustomerNotFoundException;
import com.mcb.billing.customer.exception.DuplicateEmailException;
import com.mcb.billing.customer.mapper.CustomerMapper;
import com.mcb.billing.customer.repository.CustomerRepository;
import com.mcb.billing.customer.web.dto.CreateCustomerRequestDTO;
import com.mcb.billing.customer.web.dto.CustomerResponseDTO;
import com.mcb.billing.customer.web.dto.UpdateCustomerRequestDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for customers. Orchestrates repository, mapper and number
 * generation, and enforces the domain rules.
 */
@Service
public class CustomerService {

    private final CustomerRepository repository;
    private final CustomerMapper mapper;
    private final CustomerNumberGenerator numberGenerator;

    public CustomerService(CustomerRepository repository,
                           CustomerMapper mapper,
                           CustomerNumberGenerator numberGenerator) {
        this.repository = repository;
        this.mapper = mapper;
        this.numberGenerator = numberGenerator;
    }

    /**
     * Creates a new customer: rejects duplicate emails, assigns a generated
     * customer number, and persists. The whole method runs in one transaction.
     */
    @Transactional
    public CustomerResponseDTO createCustomer(CreateCustomerRequestDTO request) {
        if (repository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }
        Customer customer = mapper.toEntity(request);
        customer.setCustomerNumber(numberGenerator.next());
        Customer saved = repository.save(customer);
        return mapper.toResponse(saved);
    }

    /** Returns a single customer by its business key, or throws if unknown. */
    @Transactional(readOnly = true)
    public CustomerResponseDTO getByCustomerNumber(String customerNumber) {
        Customer customer = requireCustomer(customerNumber);
        return mapper.toResponse(customer);
    }

    /** Returns all customers (including deactivated ones). */
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> listAllCustomers() {
        return repository.findAll().stream().map(mapper::toResponse).toList();
    }

    /** Returns only active (not soft-deleted) customers. */
    @Transactional(readOnly = true)
    public List<CustomerResponseDTO> listActiveCustomers() {
        return repository.findByActiveTrue().stream().map(mapper::toResponse).toList();
    }

    /**
     * Full-replace update of an existing customer. Rejects an email change that
     * would collide with another customer. No explicit save() is needed: the
     * loaded entity is managed, so dirty checking flushes the UPDATE on commit.
     */
    @Transactional
    public CustomerResponseDTO updateCustomer(String customerNumber, UpdateCustomerRequestDTO request) {
        Customer customer = requireCustomer(customerNumber);

        boolean emailChanged = !customer.getEmail().equals(request.email());
        if (emailChanged && repository.existsByEmail(request.email())) {
            throw new DuplicateEmailException(request.email());
        }

        mapper.updateEntity(customer, request);
        return mapper.toResponse(customer);
    }

    /** Soft-deletes a customer by setting it inactive (dirty checking flushes the UPDATE). */
    @Transactional
    public void deactivateCustomer(String customerNumber) {
        Customer customer = requireCustomer(customerNumber);
        customer.setActive(false);
    }

    private Customer requireCustomer(String customerNumber) {
        return repository.findByCustomerNumber(customerNumber)
                .orElseThrow(() -> new CustomerNotFoundException(customerNumber));
    }
}
