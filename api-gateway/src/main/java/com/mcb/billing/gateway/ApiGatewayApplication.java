package com.mcb.billing.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the API gateway - the single front door to the billing platform.
 * Routing rules and (later) security are configured declaratively; this service holds
 * no business logic or database of its own.
 */
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
