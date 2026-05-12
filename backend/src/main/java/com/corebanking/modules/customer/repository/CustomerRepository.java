package com.corebanking.modules.customer.repository;

import com.corebanking.modules.customer.entity.Customer;
import com.corebanking.modules.customer.entity.CustomerStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID>, JpaSpecificationExecutor<Customer> {

    boolean existsByNumeroDocumento(String numeroDocumento);

    boolean existsByEmailIgnoreCase(String email);

    Optional<Customer> findByNumeroDocumento(String numeroDocumento);

    long countByEstado(CustomerStatus estado);
}
