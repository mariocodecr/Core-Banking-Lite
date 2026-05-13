package com.corebanking.modules.account.repository;

import com.corebanking.modules.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID>, JpaSpecificationExecutor<Account> {

    List<Account> findByCustomerId(UUID customerId);

    Optional<Account> findByNumeroCuenta(String numeroCuenta);

    boolean existsByNumeroCuenta(String numeroCuenta);

    @Query("SELECT COUNT(a) FROM Account a WHERE a.estado <> com.corebanking.modules.account.entity.AccountStatus.CERRADA")
    long countNonClosed();

    @Query("SELECT COALESCE(SUM(a.saldo), 0) FROM Account a WHERE a.moneda = :moneda AND a.estado <> com.corebanking.modules.account.entity.AccountStatus.CERRADA")
    BigDecimal sumSaldoByMoneda(@Param("moneda") String moneda);

    /** Returns [AccountType, count, totalBalance] rows grouped by account type (excludes CERRADA). */
    @Query("SELECT a.tipo, COUNT(a), COALESCE(SUM(a.saldo), 0) FROM Account a WHERE a.estado <> com.corebanking.modules.account.entity.AccountStatus.CERRADA GROUP BY a.tipo ORDER BY a.tipo")
    List<Object[]> findRawStatsByType();
}
