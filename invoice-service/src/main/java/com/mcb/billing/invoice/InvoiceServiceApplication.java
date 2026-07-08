package com.mcb.billing.invoice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point of the invoice-service.
 *
 * <p>{@code @EnableFeignClients} activates Spring Cloud OpenFeign: interfaces
 * annotated with {@code @FeignClient} are turned into HTTP clients at runtime,
 * used here to call the customer-service.
 */
@SpringBootApplication
@EnableFeignClients
public class InvoiceServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InvoiceServiceApplication.class, args);
    }
}
