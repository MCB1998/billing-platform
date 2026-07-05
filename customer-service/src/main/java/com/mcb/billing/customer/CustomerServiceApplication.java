package com.mcb.billing.customer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the customer-service.
 *
 * @SpringBootApplication bundles three things:
 *  - @Configuration         : this class may define beans
 *  - @EnableAutoConfiguration: Spring Boot configures Tomcat, JPA, DataSource ...
 *                             automatically based on the dependencies on the classpath
 *  - @ComponentScan         : finds controllers/services/repositories in this package
 *                             and below (com.mcb.billing.customer.*)
 */
@SpringBootApplication
public class CustomerServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CustomerServiceApplication.class, args);
    }
}
