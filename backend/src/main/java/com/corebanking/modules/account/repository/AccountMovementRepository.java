package com.corebanking.modules.account.repository;

import com.corebanking.modules.account.entity.AccountMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountMovementRepository extends JpaRepository<AccountMovement, UUID> {

    Page<AccountMovement> findByAccountIdOrderByFechaMovimientoDesc(UUID accountId, Pageable pageable);

    boolean existsByReferencia(String referencia);
}
