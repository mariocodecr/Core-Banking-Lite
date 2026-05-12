package com.corebanking.modules.transfer.entity;

import com.corebanking.audit.AuditableEntity;
import com.corebanking.modules.account.entity.Account;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Financial transfer record. Immutable after creation.
 *
 * idempotencyKey prevents duplicate processing: if the same key is submitted
 * twice (e.g. network retry), the existing transfer is returned instead of
 * executing again. This guarantees exactly-once semantics at the service layer.
 *
 * referencia links this Transfer to the two AccountMovements it generated
 * (TRANSFERENCIA_SALIDA on origin, TRANSFERENCIA_ENTRADA on destination).
 */
@Entity
@Table(
    name = "transfers",
    indexes = {
        @Index(name = "idx_transfers_cuenta_origen", columnList = "cuenta_origen_id"),
        @Index(name = "idx_transfers_cuenta_destino", columnList = "cuenta_destino_id"),
        @Index(name = "idx_transfers_fecha", columnList = "fecha_transferencia")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class Transfer extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_origen_id", nullable = false)
    private Account cuentaOrigen;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cuenta_destino_id", nullable = false)
    private Account cuentaDestino;

    @Column(name = "monto", nullable = false, precision = 19, scale = 4)
    private BigDecimal monto;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private TransferStatus estado;

    /** Idempotency key provided by the client (UUID recommended). */
    @Column(name = "idempotency_key", unique = true, nullable = false, length = 100)
    private String idempotencyKey;

    /** Reference that links this transfer to its two AccountMovements. */
    @Column(name = "referencia", nullable = false, length = 50)
    private String referencia;

    /** Populated only when estado = FALLIDA. */
    @Column(name = "motivo_fallo", length = 500)
    private String motivoFallo;

    @Column(name = "fecha_transferencia", nullable = false)
    private LocalDateTime fechaTransferencia;
}
