package com.corebanking.modules.investment.config;

import com.corebanking.modules.investment.entity.Instrument;
import com.corebanking.modules.investment.entity.InstrumentType;
import com.corebanking.modules.investment.repository.InstrumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the instruments table with popular ETFs and Mutual Funds on startup.
 * Idempotent — skips symbols that already exist, but patches instrumentType and
 * lastPrice on existing rows that still have null values.
 *
 * Fallback prices are approximate values used when Alpha Vantage is unavailable
 * (free tier: 25 req/day). The real-time price is fetched and cached on first use.
 *
 * Note: Mutual fund prices from Alpha Vantage reflect the previous day's NAV.
 * Orders for mutual funds are placed as PENDING and executed by MutualFundOrderScheduler.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InstrumentDataInitializer {

    private record InstrumentEntry(String symbol, String name, InstrumentType type, double fallbackPrice) {}

    private static final List<InstrumentEntry> CATALOG = List.of(
            // ── S&P 500 ETFs ─────────────────────────────────────────────────────
            new InstrumentEntry("SPY",  "SPDR S&P 500 ETF Trust",                        InstrumentType.ETF,         572.00),
            new InstrumentEntry("VOO",  "Vanguard S&P 500 ETF",                          InstrumentType.ETF,         526.00),
            new InstrumentEntry("IVV",  "iShares Core S&P 500 ETF",                      InstrumentType.ETF,         572.00),
            // ── Nasdaq-100 ETFs ───────────────────────────────────────────────────
            new InstrumentEntry("QQQ",  "Invesco QQQ Trust (Nasdaq-100)",                InstrumentType.ETF,         490.00),
            new InstrumentEntry("QQQM", "Invesco Nasdaq-100 ETF",                        InstrumentType.ETF,         196.00),
            // ── Russell ETFs ──────────────────────────────────────────────────────
            new InstrumentEntry("IWM",  "iShares Russell 2000 ETF",                      InstrumentType.ETF,         207.00),
            new InstrumentEntry("IWF",  "iShares Russell 1000 Growth ETF",               InstrumentType.ETF,         357.00),
            new InstrumentEntry("IWD",  "iShares Russell 1000 Value ETF",                InstrumentType.ETF,         172.00),
            // ── Broad Market ETFs ─────────────────────────────────────────────────
            new InstrumentEntry("VTI",  "Vanguard Total Stock Market ETF",               InstrumentType.ETF,         259.00),
            // ── International ETFs ────────────────────────────────────────────────
            new InstrumentEntry("EFA",  "iShares MSCI EAFE ETF",                         InstrumentType.ETF,          83.00),
            new InstrumentEntry("IEMG", "iShares Core MSCI Emerging Markets ETF",        InstrumentType.ETF,          59.00),
            // ── Bond ETFs ─────────────────────────────────────────────────────────
            new InstrumentEntry("AGG",  "iShares Core U.S. Aggregate Bond ETF",          InstrumentType.ETF,          98.00),
            // ── Commodity ETFs ────────────────────────────────────────────────────
            new InstrumentEntry("GLD",  "SPDR Gold Shares",                              InstrumentType.ETF,         238.00),
            // ── Thematic ETFs ─────────────────────────────────────────────────────
            new InstrumentEntry("ARKK", "ARK Innovation ETF",                            InstrumentType.ETF,          56.00),

            // ── Index Mutual Funds (NAV — orders execute next business day) ───────
            new InstrumentEntry("VTSAX", "Vanguard Total Stock Market Index Fund Admiral", InstrumentType.MUTUAL_FUND, 138.00),
            new InstrumentEntry("VFIAX", "Vanguard 500 Index Fund Admiral Shares",         InstrumentType.MUTUAL_FUND, 512.00),
            new InstrumentEntry("FXAIX", "Fidelity 500 Index Fund",                        InstrumentType.MUTUAL_FUND, 196.00),
            new InstrumentEntry("FSKAX", "Fidelity Total Market Index Fund",               InstrumentType.MUTUAL_FUND,  96.00),
            new InstrumentEntry("SWPPX", "Schwab S&P 500 Index Fund",                      InstrumentType.MUTUAL_FUND,  78.00),
            new InstrumentEntry("SWTSX", "Schwab Total Stock Market Index Fund",           InstrumentType.MUTUAL_FUND,  79.00)
    );

    private final InstrumentRepository instrumentRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        int inserted = 0;
        int patched  = 0;

        for (InstrumentEntry entry : CATALOG) {
            if (!instrumentRepository.existsById(entry.symbol())) {
                Instrument instrument = new Instrument();
                instrument.setSymbol(entry.symbol());
                instrument.setName(entry.name());
                instrument.setInstrumentType(entry.type());
                instrument.setLastPrice(BigDecimal.valueOf(entry.fallbackPrice()));
                instrument.setActive(true);
                instrumentRepository.save(instrument);
                inserted++;
            } else {
                // Patch instrumentType and lastPrice on existing rows that predate these features
                instrumentRepository.findById(entry.symbol()).ifPresent(inst -> {
                    boolean dirty = false;
                    if (inst.getInstrumentType() == null) {
                        inst.setInstrumentType(entry.type());
                        dirty = true;
                    }
                    if (inst.getLastPrice() == null) {
                        inst.setLastPrice(BigDecimal.valueOf(entry.fallbackPrice()));
                        dirty = true;
                    }
                    if (dirty) instrumentRepository.save(inst);
                });
                patched++;
            }
        }

        if (inserted > 0 || patched > 0) {
            log.info("Instruments catalog: {} new, {} existing (type/price patched if null)", inserted, patched);
        }
    }
}
