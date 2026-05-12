package com.corebanking.modules.account.entity;

import com.corebanking.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable financial record. Movements are NEVER updated or deleted.
 * They form the audit trail required by banking regulations.
 *
 * referencia is used for idempotency in Phase 5 (Transfers).
 */
@Entity
@Table(
    name = "account_movements",
    indexes = @Index(name = "idx_movements_account_id", columnList = "account_id")
)
@Getter
@Setter
@NoArgsConstructor
public class AccountMovement extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 30)
    private MovementType tipo;

    @Column(name = "monto", nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Column(name = "saldo_anterior", nullable = false, precision = 19, scale = 4)
    private BigDecimal saldoAnterior;

    @Column(name = "saldo_posterior", nullable = false, precision = 19, scale = 4)
    private BigDecimal saldoPosterior;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    /** Idempotency key — links to the originating Transfer (Phase 5). */
    @Column(name = "referencia", length = 100)
    private String referencia;

    @Column(name = "fecha_movimiento", nullable = false)
    private LocalDateTime fechaMovimiento;
}
