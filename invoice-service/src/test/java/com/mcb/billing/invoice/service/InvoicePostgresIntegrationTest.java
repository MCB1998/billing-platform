package com.mcb.billing.invoice.service;

import com.mcb.billing.invoice.client.CustomerClient;
import com.mcb.billing.invoice.client.CustomerDTO;
import com.mcb.billing.invoice.web.dto.CreateInvoiceItemDTO;
import com.mcb.billing.invoice.web.dto.CreateInvoiceRequestDTO;
import com.mcb.billing.invoice.web.dto.InvoiceResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Integration test running against a REAL PostgreSQL (Testcontainers), with the
 * 'postgres' profile so Flyway applies V1 and Hibernate validates the schema. The
 * customer-service Feign client is mocked, so only this service's persistence is tested.
 *
 * <p>Note: with {@code disabledWithoutDocker = true} this is SKIPPED when Testcontainers
 * can't find Docker (e.g. a Windows host without the default socket exposed); it runs in
 * CI (Linux) and from a WSL shell.
 */
@SpringBootTest
@ActiveProfiles("postgres")
@Testcontainers(disabledWithoutDocker = true)
class InvoicePostgresIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    InvoiceService invoiceService;

    @MockBean
    CustomerClient customerClient;

    @Test
    void persistsInvoiceInRealPostgres() {
        when(customerClient.getCustomer("C-00001"))
                .thenReturn(new CustomerDTO("C-00001", "Acme GmbH", true));

        InvoiceResponseDTO created = invoiceService.createInvoice(
                new CreateInvoiceRequestDTO("C-00001", "EUR", LocalDate.now().plusDays(30),
                        List.of(new CreateInvoiceItemDTO("Consulting", 2, new BigDecimal("100.00")))));

        assertThat(created.invoiceNumber()).matches("INV-\\d{5}");

        InvoiceResponseDTO fetched = invoiceService.getByInvoiceNumber(created.invoiceNumber());
        assertThat(fetched.customerNumber()).isEqualTo("C-00001");
        assertThat(fetched.totalAmount()).isEqualByComparingTo("200.00");
        assertThat(fetched.items()).hasSize(1);
    }
}
