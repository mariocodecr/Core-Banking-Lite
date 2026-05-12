package com.corebanking.modules.account.entity;

import com.corebanking.audit.AuditableEntity;
import com.corebanking.modules.customer.entity.Customer;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Bank account entity.
 *
 * @Version enables JPA Optimistic Locking — if two concurrent transactions
 * try to update the same account, the second one throws
 * ObjectOptimisticLockingFailureException (HTTP 409). This prevents race
 * conditions without the cost of pessimistic locking (SELECT FOR UPDATE).
 *
 * saldo uses precision=19, scale=4 — standard for financial amounts in PostgreSQL.
 */
@Entity
@Table(
    name = "accounts",
    uniqueConstraints = @UniqueConstraint(name = "uk_accounts_numero_cuenta", columnNames = "numero_cuenta")
)
@Getter
@Setter
@NoArgsConstructor
public class Account extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "numero_cuenta", nullable = false, length = 20)
    private String numeroCuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private AccountType tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private AccountStatus estado = AccountStatus.ACTIVA;

    @Column(name = "saldo", nullable = false, precision = 19, scale = 4)
    private BigDecimal saldo = BigDecimal.ZERO;

    @Column(name = "moneda", nullable = false, length = 3)
    private String moneda = "PEN";

    @Column(name = "fecha_apertura", nullable = false)
    private LocalDate fechaApertura;

    @Column(name = "fecha_cierre")
    private LocalDate fechaCierre;

    /**
     * Optimistic locking version — incremented on every UPDATE.
     * Never expose this as an editable field via API.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
