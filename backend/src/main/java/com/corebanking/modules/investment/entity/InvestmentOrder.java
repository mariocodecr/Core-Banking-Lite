package com.corebanking.modules.investment.entity;

import com.corebanking.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable record of a BUY or SELL market order.
 * All orders are executed immediately at the price returned by Alpha Vantage.
 */
@Entity
@Table(
    name = "investment_orders",
    indexes = @Index(name = "idx_investment_orders_portfolio_id", columnList = "portfolio_id")
)
@Getter
@Setter
@NoArgsConstructor
public class InvestmentOrder extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "symbol", nullable = false)
    private Instrument instrument;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 10)
    private OrderType tipo;

    @Column(name = "shares", nullable = false, precision = 19, scale = 6)
    private BigDecimal shares;

    @Column(name = "price_per_share", nullable = false, precision = 19, scale = 4)
    private BigDecimal pricePerShare;

    /** shares × pricePerShare, rounded to 4 decimal places. */
    @Column(name = "total_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 20)
    private OrderStatus estado;

    @Column(name = "fecha_orden", nullable = false)
    private LocalDateTime fechaOrden;

    @Column(name = "error_message", length = 500)
    private String errorMessage;
}
