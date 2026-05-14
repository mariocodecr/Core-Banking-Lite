package com.corebanking.modules.investment.entity;

import com.corebanking.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Current holding of an instrument within a portfolio.
 * avgCost is updated on every BUY using weighted average cost (WAC).
 * On SELL, avgCost stays unchanged — only shares decreases.
 * A position is deleted when shares reach zero.
 */
@Entity
@Table(
    name = "positions",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_positions_portfolio_symbol",
        columnNames = {"portfolio_id", "symbol"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class Position extends AuditableEntity {

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

    /** Supports fractional shares. */
    @Column(name = "shares", nullable = false, precision = 19, scale = 6)
    private BigDecimal shares;

    /** Weighted average cost per share in USD. */
    @Column(name = "avg_cost", nullable = false, precision = 19, scale = 4)
    private BigDecimal avgCost;
}
