package com.mcb.billing.invoice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Declarative HTTP client for the customer-service. Spring Cloud OpenFeign turns
 * this interface into a working HTTP client at runtime; each method maps to a
 * customer-service endpoint via the familiar Spring MVC annotations.
 *
 * <p>The target address comes from the {@code customer-service.url} property. On a
 * non-2xx response Feign throws a {@code FeignException} (e.g. NotFound for 404),
 * which the service layer translates into a domain error.
 */
@FeignClient(name = "customer-service", url = "${customer-service.url}")
public interface CustomerClient {

    /** Fetches a customer by its business key; 404 -> FeignException.NotFound. */
    @GetMapping("/customers/{customerNumber}")
    CustomerDTO getCustomer(@PathVariable("customerNumber") String customerNumber);
}
