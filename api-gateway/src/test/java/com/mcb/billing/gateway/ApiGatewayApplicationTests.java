package com.mcb.billing.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Smoke test: verifies the Spring (WebFlux) application context starts. With no routes
 * or external dependencies yet, this just proves the gateway wiring is sound.
 */
@SpringBootTest
class ApiGatewayApplicationTests {

    @Test
    void contextLoads() {
    }
}
