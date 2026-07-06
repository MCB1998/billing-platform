package com.mcb.billing.customer.repository;

import com.mcb.billing.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Data-access layer for {@link Customer}.
 *
 * <p>By extending {@link JpaRepository} we inherit the standard CRUD operations
 * (save, findById, findAll, deleteById, ...). The methods below are "derived
 * queries": Spring Data generates their implementation from the method name at
 * startup, so we write neither SQL nor an implementation.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /** Look up a customer by its business key. */
    Optional<Customer> findByCustomerNumber(String customerNumber);

    /** All customers that are not soft-deleted. */
    List<Customer> findByActiveTrue();

    /** Uniqueness checks used by the service before inserting. */
    boolean existsByEmail(String email);

    boolean existsByCustomerNumber(String customerNumber);
}
