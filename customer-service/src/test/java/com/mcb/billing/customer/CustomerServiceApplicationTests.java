package com.mcb.billing.customer;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: verifies that the Spring application context starts up successfully.
 * If any bean is misconfigured or a dependency is missing, this test fails — a
 * cheap early safety net that runs in CI on every push and pull request.
 */
@SpringBootTest
class CustomerServiceApplicationTests {

    @Test
    void contextLoads() {
        // Intentionally empty: the test passes if the context above loads without error.
    }
}
