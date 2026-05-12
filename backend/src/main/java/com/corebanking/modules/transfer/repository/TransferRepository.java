package com.corebanking.modules.transfer.repository;

import com.corebanking.modules.transfer.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);

    Page<Transfer> findAllByOrderByFechaTransferenciaDesc(Pageable pageable);

    /**
     * Calculates the total amount transferred from a given account on the current day.
     * Used to enforce daily transfer limits.
     */
    @Query("""
        SELECT COALESCE(SUM(t.monto), 0)
        FROM Transfer t
        WHERE t.cuentaOrigen.id = :accountId
          AND t.estado = com.corebanking.modules.transfer.entity.TransferStatus.COMPLETADA
          AND t.fechaTransferencia >= :startOfDay
        """)
    BigDecimal sumCompletedTransfersByAccountAndDate(
            @Param("accountId") UUID accountId,
            @Param("startOfDay") LocalDateTime startOfDay);
}
