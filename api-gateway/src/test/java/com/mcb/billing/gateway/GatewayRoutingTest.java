package com.mcb.billing.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the declarative routes are wired as configured (id -> target URI), reading
 * them from the RouteLocator. This exercises the routing configuration without needing
 * the downstream services to be running.
 */
@SpringBootTest
class GatewayRoutingTest {

    @Autowired
    RouteLocator routeLocator;

    @Test
    void routesTheThreeBackendServices() {
        List<Route> routes = routeLocator.getRoutes().collectList().block();
        assertThat(routes).isNotNull();

        Map<String, String> targetById = routes.stream()
                .collect(Collectors.toMap(Route::getId, route -> route.getUri().toString()));

        assertThat(targetById)
                .containsEntry("customer-service", "http://localhost:8081")
                .containsEntry("invoice-service", "http://localhost:8082")
                .containsEntry("notification-service", "http://localhost:8083");
    }
}
