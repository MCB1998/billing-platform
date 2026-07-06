package com.mcb.billing.customer.service;

import com.mcb.billing.customer.exception.CustomerNotFoundException;
import com.mcb.billing.customer.exception.DuplicateEmailException;
import com.mcb.billing.customer.web.dto.CreateCustomerRequestDTO;
import com.mcb.billing.customer.web.dto.CustomerResponseDTO;
import com.mcb.billing.customer.web.dto.UpdateCustomerRequestDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class CustomerServiceTest {

    @Autowired
    CustomerService customerService;

    private CustomerResponseDTO create(String companyName, String email) {
        return customerService.createCustomer(
                new CreateCustomerRequestDTO(companyName, email,
                        null, null, null, null, null, null, null));
    }

    @Test
    void createsCustomersWithIncrementingNumbers() {
        CustomerResponseDTO first = create("Acme GmbH", "acme@example.com");
        CustomerResponseDTO second = create("Globex GmbH", "globex@example.com");

        assertThat(first.customerNumber()).matches("C-\\d{5}");
        assertThat(second.customerNumber()).matches("C-\\d{5}");

        long firstNum = Long.parseLong(first.customerNumber().substring(2));
        long secondNum = Long.parseLong(second.customerNumber().substring(2));
        assertThat(secondNum).isEqualTo(firstNum + 1);

        assertThat(first.active()).isTrue();
        assertThat(first.createdAt()).isNotNull();
    }

    @Test
    void rejectsDuplicateEmail() {
        create("Acme GmbH", "duplicate@example.com");

        assertThatThrownBy(() -> create("Other GmbH", "duplicate@example.com"))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void getByCustomerNumberReturnsCustomer() {
        CustomerResponseDTO created = create("Get GmbH", "get@example.com");

        CustomerResponseDTO fetched = customerService.getByCustomerNumber(created.customerNumber());

        assertThat(fetched.companyName()).isEqualTo("Get GmbH");
        assertThat(fetched.email()).isEqualTo("get@example.com");
    }

    @Test
    void getByUnknownNumberThrowsNotFound() {
        assertThatThrownBy(() -> customerService.getByCustomerNumber("C-99999"))
                .isInstanceOf(CustomerNotFoundException.class);
    }

    @Test
    void updateChangesFieldsAndPersists() {
        CustomerResponseDTO created = create("Old Name", "update@example.com");
        UpdateCustomerRequestDTO update = new UpdateCustomerRequestDTO(
                "New Name", "update@example.com", "Jane Doe", "12345",
                "Main St 1", "10115", "Berlin", "DE", "DE123456789");

        CustomerResponseDTO updated = customerService.updateCustomer(created.customerNumber(), update);
        assertThat(updated.companyName()).isEqualTo("New Name");
        assertThat(updated.contactPerson()).isEqualTo("Jane Doe");
        assertThat(updated.city()).isEqualTo("Berlin");

        // Re-fetch to prove dirty checking persisted the change (no explicit save()).
        CustomerResponseDTO refetched = customerService.getByCustomerNumber(created.customerNumber());
        assertThat(refetched.companyName()).isEqualTo("New Name");
    }

    @Test
    void updateToExistingEmailIsRejected() {
        create("A GmbH", "taken@example.com");
        CustomerResponseDTO b = create("B GmbH", "b@example.com");

        UpdateCustomerRequestDTO update = new UpdateCustomerRequestDTO(
                "B GmbH", "taken@example.com", null, null, null, null, null, null, null);

        assertThatThrownBy(() -> customerService.updateCustomer(b.customerNumber(), update))
                .isInstanceOf(DuplicateEmailException.class);
    }

    @Test
    void deactivateExcludesFromActiveList() {
        CustomerResponseDTO created = create("Deact GmbH", "deact@example.com");

        customerService.deactivateCustomer(created.customerNumber());

        assertThat(customerService.getByCustomerNumber(created.customerNumber()).active()).isFalse();

        boolean inActiveList = customerService.listActiveCustomers().stream()
                .anyMatch(c -> c.customerNumber().equals(created.customerNumber()));
        boolean inAllList = customerService.listAllCustomers().stream()
                .anyMatch(c -> c.customerNumber().equals(created.customerNumber()));

        assertThat(inActiveList).isFalse();
        assertThat(inAllList).isTrue();
    }
}
