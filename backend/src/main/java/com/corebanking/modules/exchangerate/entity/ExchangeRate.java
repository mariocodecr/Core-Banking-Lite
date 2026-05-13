package com.corebanking.modules.exchangerate.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Persists the last known CRC rate per currency fetched from BCCR.
 * Acts as a fallback when BCCR is unavailable or hasn't published today's rates yet.
 *
 * One row per currency (USD, EUR). Updated every time a successful BCCR call is made.
 */
@Entity
@Table(name = "exchange_rates")
@Getter
@Setter
@NoArgsConstructor
public class ExchangeRate {

    /** Currency code (USD, EUR). CRC is always 1 and is never stored. */
    @Id
    @Column(name = "currency", length = 3, nullable = false)
    private String currency;

    /** How many CRC equal 1 unit of this currency (venta rate). */
    @Column(name = "crc_rate", nullable = false, precision = 19, scale = 6)
    private BigDecimal crcRate;

    /** The date BCCR published this rate. */
    @Column(name = "published_date", nullable = false)
    private LocalDate publishedDate;

    /** When we last successfully fetched this rate from BCCR. */
    @Column(name = "fetched_at", nullable = false)
    private LocalDateTime fetchedAt;
}
