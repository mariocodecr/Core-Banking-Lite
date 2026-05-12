package com.corebanking.modules.account.repository;

import com.corebanking.modules.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {

    List<Account> findByCustomerId(UUID customerId);

    Optional<Account> findByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuenta(String numeroCuenta);
}
