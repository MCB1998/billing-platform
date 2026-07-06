package com.mcb.billing.customer.mapper;

import com.mcb.billing.customer.domain.Customer;
import com.mcb.billing.customer.web.dto.CreateCustomerRequestDTO;
import com.mcb.billing.customer.web.dto.CustomerResponseDTO;
import com.mcb.billing.customer.web.dto.UpdateCustomerRequestDTO;
import org.springframework.stereotype.Component;

/**
 * Translates between the Customer entity (persistence layer) and its DTOs
 * (API layer). Registered as a Spring bean so it can be injected into the service.
 */
@Component
public class CustomerMapper {

    /**
     * Maps the input DTO to a new entity. The {@code customerNumber} is intentionally
     * not set here — the service assigns it. {@code id}, timestamps and {@code active}
     * are handled by JPA / entity defaults.
     */
    public Customer toEntity(CreateCustomerRequestDTO dto) {
        Customer customer = new Customer(dto.companyName(), dto.email());
        customer.setContactPerson(dto.contactPerson());
        customer.setPhone(dto.phone());
        customer.setStreet(dto.street());
        customer.setPostalCode(dto.postalCode());
        customer.setCity(dto.city());
        customer.setCountry(dto.country());
        customer.setVatId(dto.vatId());
        return customer;
    }

    /**
     * Applies the update DTO to an existing (managed) entity. Does not touch the
     * customerNumber, id, active flag or timestamps. Because the entity is managed,
     * Hibernate's dirty checking will flush the UPDATE on transaction commit.
     */
    public void updateEntity(Customer customer, UpdateCustomerRequestDTO dto) {
        customer.setCompanyName(dto.companyName());
        customer.setEmail(dto.email());
        customer.setContactPerson(dto.contactPerson());
        customer.setPhone(dto.phone());
        customer.setStreet(dto.street());
        customer.setPostalCode(dto.postalCode());
        customer.setCity(dto.city());
        customer.setCountry(dto.country());
        customer.setVatId(dto.vatId());
    }

    /** Maps the entity to the output DTO (customerNumber as external id, no internal id). */
    public CustomerResponseDTO toResponse(Customer customer) {
        return new CustomerResponseDTO(
                customer.getCustomerNumber(),
                customer.getCompanyName(),
                customer.getContactPerson(),
                customer.getEmail(),
                customer.getPhone(),
                customer.getStreet(),
                customer.getPostalCode(),
                customer.getCity(),
                customer.getCountry(),
                customer.getVatId(),
                customer.isActive(),
                customer.getCreatedAt(),
                customer.getUpdatedAt()
        );
    }
}
