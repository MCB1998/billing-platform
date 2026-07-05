package com.mcb.billing.customer.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

/**
 * Minimal health/liveness endpoint so we can immediately see that the service
 * is running. To be replaced/extended later with real endpoints (and Spring Boot
 * Actuator for /actuator/health).
 *
 * @RestController = @Controller + @ResponseBody: return values are written
 * directly as JSON into the HTTP response (via Jackson).
 */
@RestController
public class PingController {

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        return Map.of(
                "service", "customer-service",
                "status", "UP",
                "time", Instant.now().toString()
        );
    }
}
