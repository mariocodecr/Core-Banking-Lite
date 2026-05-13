package com.corebanking.modules.exchangerate.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

public interface ExchangeRateService {

    /**
     * Returns how many CRC are worth 1 unit of the given currency.
     * e.g. getCrcRate("USD") ≈ 507.50 → 1 USD = 507.50 CRC
     *      getCrcRate("CRC") = 1.00
     */
    BigDecimal getCrcRate(String currency);

    /**
     * Rate to convert 1 unit of <from> into <to>.
     * Uses CRC as the pivot currency:  rate = getCrcRate(from) / getCrcRate(to)
     */
    default BigDecimal getRate(String from, String to) {
        if (from.equals(to)) return BigDecimal.ONE;
        return getCrcRate(from).divide(getCrcRate(to), 6, RoundingMode.HALF_UP);
    }

    /**
     * Converts an amount from one currency to another.
     */
    default BigDecimal convert(BigDecimal amount, String from, String to) {
        return amount.multiply(getRate(from, to)).setScale(4, RoundingMode.HALF_UP);
    }
}
