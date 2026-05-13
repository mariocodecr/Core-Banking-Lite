package com.corebanking.modules.transfer.repository;

import com.corebanking.modules.transfer.entity.Transfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    @Query("""
        SELECT COUNT(t) FROM Transfer t
        WHERE t.estado = com.corebanking.modules.transfer.entity.TransferStatus.COMPLETADA
          AND t.fechaTransferencia >= :since
        """)
    long countCompletedSince(@Param("since") LocalDateTime since);

    @Query("""
        SELECT COALESCE(SUM(t.monto), 0) FROM Transfer t
        WHERE t.estado = com.corebanking.modules.transfer.entity.TransferStatus.COMPLETADA
          AND t.fechaTransferencia >= :since
        """)
    BigDecimal sumVolumeSince(@Param("since") LocalDateTime since);

    /** Returns [date (LocalDate), count (Long), volume (BigDecimal)] rows for completed transfers. */
    @Query(value = """
        SELECT CAST(t.fecha_transferencia AS DATE)  AS date,
               COUNT(*)                             AS count,
               COALESCE(SUM(t.monto), 0)           AS volume
        FROM transfers t
        WHERE t.estado = 'COMPLETADA'
          AND t.fecha_transferencia >= :startDate
        GROUP BY CAST(t.fecha_transferencia AS DATE)
        ORDER BY CAST(t.fecha_transferencia AS DATE)
        """, nativeQuery = true)
    List<Object[]> findRawDailyStats(@Param("startDate") LocalDateTime startDate);
}
