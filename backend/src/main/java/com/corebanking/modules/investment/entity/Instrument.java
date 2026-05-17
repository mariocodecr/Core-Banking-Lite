package com.corebanking.modules.investment.entity;

import com.corebanking.audit.AuditableEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;



/**
 * Reference data: available ETF instruments.
 * Pre-seeded by InstrumentDataInitializer. lastPrice is updated on every
 * successful Alpha Vantage fetch and used as fallback when the API is unavailable.
 */
@Entity
@Table(name = "instruments")
@Getter
@Setter
@NoArgsConstructor
public class Instrument extends AuditableEntity {

    @Id
    @Column(name = "symbol", length = 10)
    private String symbol;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "last_price", precision = 19, scale = 4)
    private BigDecimal lastPrice;

    @Column(name = "last_price_updated")
    private LocalDateTime lastPriceUpdated;

    /**
     * ETF: trades on exchange, intraday price. Executed immediately.
     * MUTUAL_FUND: NAV priced once per day. Orders are PENDING until the scheduler runs.
     * Column default 'ETF' ensures existing rows are backfilled on ddl-auto: update.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "instrument_type", nullable = false, length = 20,
            columnDefinition = "VARCHAR(20) DEFAULT 'ETF'")
    private InstrumentType instrumentType = InstrumentType.ETF;

    @Column(name = "active", nullable = false)
    private boolean active = true;
}
