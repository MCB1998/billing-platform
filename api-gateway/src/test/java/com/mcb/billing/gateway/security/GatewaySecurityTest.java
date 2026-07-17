package com.mcb.billing.gateway.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

/**
 * Verifies the SecurityWebFilterChain rules. Runs fully offline: the unauthenticated
 * cases are rejected before any token is decoded, and the authenticated case uses
 * mockJwt() - so neither Keycloak nor the downstream services need to be running.
 *
 * <p>The route table is used as the probe because the gateway answers it locally
 * (no proxying), which keeps the assertions deterministic.
 */
@SpringBootTest
@AutoConfigureWebTestClient
class GatewaySecurityTest {

    @Autowired
    WebTestClient webTestClient;

    @Test
    void healthIsPublic() {
        webTestClient.get().uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void routeTableRequiresAToken() {
        webTestClient.get().uri("/actuator/gateway/routes")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void routeTableIsAllowedWithAValidToken() {
        webTestClient.mutateWith(mockJwt())
                .get().uri("/actuator/gateway/routes")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void proxiedServiceRoutesRequireAToken() {
        webTestClient.get().uri("/customers")
                .exchange()
                .expectStatus().isUnauthorized();
    }
}
