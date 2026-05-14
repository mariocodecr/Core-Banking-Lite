package com.corebanking.modules.investment.service;

import com.corebanking.modules.account.entity.MovementType;
import com.corebanking.modules.account.service.AccountService;
import com.corebanking.modules.investment.entity.InvestmentOrder;
import com.corebanking.modules.investment.entity.OrderStatus;
import com.corebanking.modules.investment.entity.OrderType;
import com.corebanking.modules.investment.repository.InvestmentOrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Processes PENDING mutual fund orders.
 *
 * Real markets execute mutual fund orders at EOD NAV. Here we simulate that by
 * running on a configurable interval (default: every 5 minutes in dev).
 *
 * - PENDING BUY  → position updated (shares added), order → EXECUTED
 * - PENDING SELL → account credited (funds released), order → EXECUTED
 *
 * If execution fails, the order is marked FAILED and:
 *   - BUY: account is refunded
 *   - SELL: (position was already decremented — logged as unrecoverable in demo)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MutualFundOrderScheduler {

    private final InvestmentOrderRepository investmentOrderRepository;
    private final InvestmentServiceImpl     investmentService;
    private final AccountService            accountService;

    @Scheduled(fixedRateString = "${app.investments.pending-execution-interval-ms:300000}")
    @Transactional
    public void executePendingOrders() {
        List<InvestmentOrder> pending = investmentOrderRepository.findByEstadoWithDetails(OrderStatus.PENDING);

        if (pending.isEmpty()) return;

        log.info("MutualFundScheduler: processing {} pending order(s)", pending.size());

        for (InvestmentOrder order : pending) {
            try {
                if (order.getTipo() == OrderType.BUY) {
                    executePendingBuy(order);
                } else {
                    executePendingSell(order);
                }
                order.setEstado(OrderStatus.EXECUTED);
                investmentOrderRepository.save(order);
                log.info("Order {} [{}] executed: {} {} shares",
                        order.getId(), order.getTipo(), order.getShares(), order.getInstrument().getSymbol());

            } catch (Exception ex) {
                log.error("Failed to execute pending order {} — marking FAILED: {}", order.getId(), ex.getMessage());
                order.setEstado(OrderStatus.FAILED);
                order.setErrorMessage(ex.getMessage());
                investmentOrderRepository.save(order);

                // Refund account on BUY failure
                if (order.getTipo() == OrderType.BUY) {
                    try {
                        accountService.credit(
                                order.getPortfolio().getAccount().getId(),
                                order.getTotalAmount(),
                                "Reembolso orden fallida " + order.getInstrument().getSymbol(),
                                "REFUND-" + order.getId().toString().substring(0, 8).toUpperCase(),
                                MovementType.DEPOSITO);
                    } catch (Exception refundEx) {
                        log.error("CRITICAL: refund failed for order {} — manual intervention needed", order.getId(), refundEx);
                    }
                }
            }
        }
    }

    // ── Private ───────────────────────────────────────────────────────────────

    private void executePendingBuy(InvestmentOrder order) {
        investmentService.applyBuyToPosition(
                order.getPortfolio(),
                order.getInstrument(),
                order.getShares(),
                order.getPricePerShare());
    }

    private void executePendingSell(InvestmentOrder order) {
        accountService.credit(
                order.getPortfolio().getAccount().getId(),
                order.getTotalAmount(),
                "Venta " + order.getShares() + " " + order.getInstrument().getSymbol() + " [EJECUTADA]",
                "SCHED-" + order.getId().toString().substring(0, 8).toUpperCase(),
                MovementType.VENTA_INVERSION);
    }
}
