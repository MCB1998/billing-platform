package com.mcb.billing.customer.service;

import com.mcb.billing.customer.web.dto.CreateCustomerRequestDTO;
import com.mcb.billing.customer.web.dto.CustomerResponseDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test running against a REAL PostgreSQL started in a throwaway
 * Docker container (Testcontainers). Activates the 'postgres' profile, so Flyway
 * applies V1 and Hibernate runs with ddl-auto=validate against the container -
 * exactly the setup used in production. Requires Docker to be available.
 *
 * <p>Note: with {@code disabledWithoutDocker = true} this test is SKIPPED when
 * Testcontainers can't find a Docker environment. On Windows + Docker Desktop the
 * default Docker socket is often not exposed, so it skips when run from a Windows
 * shell; it runs normally in CI (Linux) and from a WSL shell with Docker integration.
 */
@SpringBootTest
@ActiveProfiles("postgres")
@Testcontainers(disabledWithoutDocker = true)   // skip (don't fail) when Testcontainers can't find Docker; runs in CI
class CustomerPostgresIntegrationTest {

    @Container
    @ServiceConnection   // Spring Boot wires spring.datasource to this container automatically
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    CustomerService customerService;

    @Test
    void persistsAndReadsCustomerInRealPostgres() {
        CustomerResponseDTO created = customerService.createCustomer(
                new CreateCustomerRequestDTO("Testcontainers GmbH", "tc@example.com",
                        null, null, null, null, null, null, null));

        assertThat(created.customerNumber()).matches("C-\\d{5}");
        assertThat(created.active()).isTrue();

        CustomerResponseDTO fetched = customerService.getByCustomerNumber(created.customerNumber());
        assertThat(fetched.companyName()).isEqualTo("Testcontainers GmbH");
        assertThat(fetched.email()).isEqualTo("tc@example.com");
    }
}
