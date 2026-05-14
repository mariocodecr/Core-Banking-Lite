package com.corebanking.modules.investment.service;

import com.corebanking.exception.BusinessException;
import com.corebanking.exception.ErrorCode;
import com.corebanking.exception.ResourceNotFoundException;
import com.corebanking.modules.account.entity.MovementType;
import com.corebanking.modules.account.repository.AccountRepository;
import com.corebanking.modules.account.service.AccountService;
import com.corebanking.modules.investment.dto.*;
import com.corebanking.modules.investment.entity.*;
import com.corebanking.modules.investment.mapper.InvestmentMapper;
import com.corebanking.modules.investment.repository.*;
import com.corebanking.shared.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class InvestmentServiceImpl implements InvestmentService {

    private final AccountService            accountService;
    private final AccountRepository         accountRepository;
    private final InstrumentRepository      instrumentRepository;
    private final PortfolioRepository       portfolioRepository;
    private final PositionRepository        positionRepository;
    private final InvestmentOrderRepository investmentOrderRepository;
    private final InvestmentMapper          investmentMapper;
    private final MarketDataService         marketDataService;

    // ── Queries ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<InstrumentResponse> getAvailableInstruments() {
        return investmentMapper.toInstrumentResponseList(instrumentRepository.findAllByActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public InstrumentResponse getInstrumentQuote(String symbol) {
        Instrument instrument = findActiveInstrument(symbol);
        BigDecimal price = marketDataService.getPrice(symbol);
        InstrumentResponse response = investmentMapper.toInstrumentResponse(instrument);
        response.setLastPrice(price);
        response.setLastPriceUpdated(LocalDateTime.now());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolio(UUID accountId) {
        var account = accountService.findById(accountId);
        Portfolio portfolio = portfolioRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "accountId", accountId));

        List<Position> positions = positionRepository.findByPortfolioId(portfolio.getId());

        List<PositionResponse> positionResponses = positions.stream()
                .map(pos -> buildPositionResponse(pos, marketDataService.getPrice(pos.getInstrument().getSymbol())))
                .toList();

        BigDecimal totalInvested     = sum(positionResponses, PositionResponse::getInvested);
        BigDecimal totalCurrentValue = sum(positionResponses, PositionResponse::getCurrentValue);
        BigDecimal totalPnl          = totalCurrentValue.subtract(totalInvested);
        BigDecimal totalPnlPercent   = pnlPercent(totalPnl, totalInvested);

        PortfolioResponse response = new PortfolioResponse();
        response.setId(portfolio.getId());
        response.setAccountId(accountId);
        response.setNumeroCuenta(account.getNumeroCuenta());
        response.setMoneda("USD");
        response.setTotalInvested(totalInvested);
        response.setTotalCurrentValue(totalCurrentValue);
        response.setTotalPnl(totalPnl);
        response.setTotalPnlPercent(totalPnlPercent);
        response.setPositions(positionResponses);
        response.setCreatedAt(portfolio.getCreatedAt());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<InvestmentOrderResponse> getOrderHistory(UUID accountId, Pageable pageable) {
        Portfolio portfolio = portfolioRepository.findByAccountId(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "accountId", accountId));
        var page = investmentOrderRepository.findByPortfolioIdOrderByFechaOrdenDesc(portfolio.getId(), pageable);
        return PagedResponse.from(page.map(investmentMapper::toOrderResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public PortfolioResponse getCombinedPortfolio() {
        List<Position> all = positionRepository.findAllWithInstrument();

        // Group by symbol — merge positions from different portfolios
        Map<String, PositionResponse> merged = new java.util.LinkedHashMap<>();
        for (Position pos : all) {
            String     symbol = pos.getInstrument().getSymbol();
            BigDecimal price;
            try {
                price = marketDataService.getPrice(symbol); // Redis cache (1h TTL) — fast
            } catch (Exception e) {
                // fallback: use DB lastPrice, or avgCost if that's also null
                price = pos.getInstrument().getLastPrice() != null
                        ? pos.getInstrument().getLastPrice()
                        : pos.getAvgCost();
            }

            final BigDecimal finalPrice = price;
            merged.merge(symbol, buildPositionResponse(pos, price), (existing, incoming) -> {
                BigDecimal totalShares = existing.getShares().add(incoming.getShares());
                BigDecimal newAvgCost  = existing.getShares().multiply(existing.getAvgCost())
                        .add(incoming.getShares().multiply(incoming.getAvgCost()))
                        .divide(totalShares, 4, RoundingMode.HALF_UP);
                BigDecimal currentVal  = totalShares.multiply(finalPrice).setScale(4, RoundingMode.HALF_UP);
                BigDecimal invested    = totalShares.multiply(newAvgCost).setScale(4, RoundingMode.HALF_UP);
                BigDecimal pnl         = currentVal.subtract(invested);

                PositionResponse pr = new PositionResponse();
                pr.setSymbol(symbol);
                pr.setInstrumentName(existing.getInstrumentName());
                pr.setShares(totalShares);
                pr.setAvgCost(newAvgCost);
                pr.setCurrentPrice(finalPrice);
                pr.setCurrentValue(currentVal);
                pr.setInvested(invested);
                pr.setPnl(pnl);
                pr.setPnlPercent(pnlPercent(pnl, invested));
                return pr;
            });
        }

        List<PositionResponse> positions = new java.util.ArrayList<>(merged.values());
        BigDecimal totalInvested     = sum(positions, PositionResponse::getInvested);
        BigDecimal totalCurrentValue = sum(positions, PositionResponse::getCurrentValue);
        BigDecimal totalPnl          = totalCurrentValue.subtract(totalInvested);

        PortfolioResponse response = new PortfolioResponse();
        response.setId(null);
        response.setAccountId(null);
        response.setNumeroCuenta("—");
        response.setMoneda("USD");
        response.setTotalInvested(totalInvested);
        response.setTotalCurrentValue(totalCurrentValue);
        response.setTotalPnl(totalPnl);
        response.setTotalPnlPercent(pnlPercent(totalPnl, totalInvested));
        response.setPositions(positions);
        response.setCreatedAt(null);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public InvestmentSummaryResponse getInvestmentSummary() {
        List<Position> positions = positionRepository.findAllWithInstrument();

        BigDecimal totalInvested     = BigDecimal.ZERO;
        BigDecimal totalCurrentValue = BigDecimal.ZERO;

        for (Position pos : positions) {
            BigDecimal price = pos.getInstrument().getLastPrice();
            if (price == null) price = pos.getAvgCost(); // fallback: no loss/gain shown

            totalInvested     = totalInvested.add(pos.getShares().multiply(pos.getAvgCost()));
            totalCurrentValue = totalCurrentValue.add(pos.getShares().multiply(price));
        }

        BigDecimal totalPnl        = totalCurrentValue.subtract(totalInvested);
        BigDecimal totalPnlPercent = pnlPercent(totalPnl, totalInvested);

        long activePortfolios = portfolioRepository.count();

        InvestmentSummaryResponse response = new InvestmentSummaryResponse();
        response.setTotalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP));
        response.setTotalCurrentValue(totalCurrentValue.setScale(2, RoundingMode.HALF_UP));
        response.setTotalPnl(totalPnl.setScale(2, RoundingMode.HALF_UP));
        response.setTotalPnlPercent(totalPnlPercent);
        response.setActivePortfolios((int) activePortfolios);
        response.setTotalPositions(positions.size());
        return response;
    }

    // ── Commands ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public InvestmentOrderResponse buy(BuyOrderRequest request) {
        var account = accountService.findById(request.getAccountId());
        validateUsdAccount(account.getMoneda(), account.getNumeroCuenta());

        Instrument instrument  = findActiveInstrument(request.getSymbol());
        BigDecimal price       = marketDataService.getPrice(request.getSymbol());
        BigDecimal totalAmount = request.getShares().multiply(price).setScale(4, RoundingMode.HALF_UP);
        String     reference   = buildReference("BUY", request.getSymbol());

        // Debit account immediately regardless of instrument type (reserves funds)
        accountService.debit(
                request.getAccountId(), totalAmount,
                "Compra " + request.getShares() + " " + request.getSymbol() + " @ $" + price,
                reference, MovementType.COMPRA_INVERSION);

        Portfolio portfolio = getOrCreatePortfolio(request.getAccountId());

        if (instrument.getInstrumentType() == InstrumentType.MUTUAL_FUND) {
            // Mutual fund: order is PENDING — position update deferred to scheduler
            InvestmentOrder order = buildOrder(portfolio, instrument, OrderType.BUY,
                    request.getShares(), price, totalAmount, OrderStatus.PENDING);
            InvestmentOrder saved = investmentOrderRepository.save(order);
            log.info("BUY [PENDING] mutual fund: {} {} @ NAV ${} — account {}",
                    request.getShares(), request.getSymbol(), price, account.getNumeroCuenta());
            return investmentMapper.toOrderResponse(saved);
        }

        // ETF: execute immediately
        applyBuyToPosition(portfolio, instrument, request.getShares(), price);

        InvestmentOrder order = buildOrder(portfolio, instrument, OrderType.BUY,
                request.getShares(), price, totalAmount, OrderStatus.EXECUTED);
        InvestmentOrder saved = investmentOrderRepository.save(order);
        log.info("BUY executed ETF: {} {} @ ${} — account {}",
                request.getShares(), request.getSymbol(), price, account.getNumeroCuenta());
        return investmentMapper.toOrderResponse(saved);
    }

    @Override
    @Transactional
    public InvestmentOrderResponse sell(SellOrderRequest request) {
        var account = accountService.findById(request.getAccountId());
        validateUsdAccount(account.getMoneda(), account.getNumeroCuenta());

        Portfolio portfolio = portfolioRepository.findByAccountId(request.getAccountId())
                .orElseThrow(() -> new ResourceNotFoundException("Portfolio", "accountId", request.getAccountId()));

        Instrument instrument = findActiveInstrument(request.getSymbol());

        Position position = positionRepository
                .findByPortfolioIdAndInstrumentSymbol(portfolio.getId(), request.getSymbol())
                .orElseThrow(() -> new BusinessException(ErrorCode.INSUFFICIENT_SHARES,
                        "No position found for " + request.getSymbol()));

        if (position.getShares().compareTo(request.getShares()) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_SHARES,
                    "Insufficient shares. Available: " + position.getShares() + ", Requested: " + request.getShares());
        }

        BigDecimal price       = marketDataService.getPrice(request.getSymbol());
        BigDecimal totalAmount = request.getShares().multiply(price).setScale(4, RoundingMode.HALF_UP);

        // Decrement position immediately regardless of type (prevents double-selling)
        decrementPosition(position, request.getShares());

        if (instrument.getInstrumentType() == InstrumentType.MUTUAL_FUND) {
            // Mutual fund: account credit deferred to scheduler
            InvestmentOrder order = buildOrder(portfolio, instrument, OrderType.SELL,
                    request.getShares(), price, totalAmount, OrderStatus.PENDING);
            InvestmentOrder saved = investmentOrderRepository.save(order);
            log.info("SELL [PENDING] mutual fund: {} {} @ NAV ${} — account {}",
                    request.getShares(), request.getSymbol(), price, account.getNumeroCuenta());
            return investmentMapper.toOrderResponse(saved);
        }

        // ETF: credit account immediately
        String reference = buildReference("SELL", request.getSymbol());
        accountService.credit(
                request.getAccountId(), totalAmount,
                "Venta " + request.getShares() + " " + request.getSymbol() + " @ $" + price,
                reference, MovementType.VENTA_INVERSION);

        InvestmentOrder order = buildOrder(portfolio, instrument, OrderType.SELL,
                request.getShares(), price, totalAmount, OrderStatus.EXECUTED);
        InvestmentOrder saved = investmentOrderRepository.save(order);
        log.info("SELL executed ETF: {} {} @ ${} — account {}",
                request.getShares(), request.getSymbol(), price, account.getNumeroCuenta());
        return investmentMapper.toOrderResponse(saved);
    }

    // ── Package-private helpers (used by MutualFundOrderScheduler) ───────────

    void applyBuyToPosition(Portfolio portfolio, Instrument instrument, BigDecimal shares, BigDecimal price) {
        Position position = positionRepository
                .findByPortfolioIdAndInstrumentSymbol(portfolio.getId(), instrument.getSymbol())
                .orElseGet(() -> {
                    Position p = new Position();
                    p.setPortfolio(portfolio);
                    p.setInstrument(instrument);
                    p.setShares(BigDecimal.ZERO);
                    p.setAvgCost(BigDecimal.ZERO);
                    return p;
                });

        BigDecimal newTotalShares = position.getShares().add(shares);
        BigDecimal newAvgCost = position.getShares().multiply(position.getAvgCost())
                .add(shares.multiply(price))
                .divide(newTotalShares, 4, RoundingMode.HALF_UP);
        position.setShares(newTotalShares);
        position.setAvgCost(newAvgCost);
        positionRepository.save(position);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void decrementPosition(Position position, BigDecimal shares) {
        BigDecimal remaining = position.getShares().subtract(shares);
        if (remaining.compareTo(BigDecimal.ZERO) == 0) {
            positionRepository.delete(position);
        } else {
            position.setShares(remaining);
            positionRepository.save(position);
        }
    }

    private Instrument findActiveInstrument(String symbol) {
        return instrumentRepository.findBySymbolAndActiveTrue(symbol.toUpperCase())
                .orElseThrow(() -> new ResourceNotFoundException("Instrument", "symbol", symbol));
    }

    private Portfolio getOrCreatePortfolio(UUID accountId) {
        return portfolioRepository.findByAccountId(accountId).orElseGet(() -> {
            Portfolio p = new Portfolio();
            p.setAccount(accountRepository.findById(accountId)
                    .orElseThrow(() -> new ResourceNotFoundException("Account", "id", accountId)));
            return portfolioRepository.save(p);
        });
    }

    private void validateUsdAccount(String moneda, String numeroCuenta) {
        if (!"USD".equals(moneda)) {
            throw new BusinessException(ErrorCode.ACCOUNT_CURRENCY_NOT_USD,
                    "Investment accounts must be denominated in USD. Account " + numeroCuenta + " is in " + moneda);
        }
    }

    private InvestmentOrder buildOrder(Portfolio portfolio, Instrument instrument,
                                       OrderType tipo, BigDecimal shares,
                                       BigDecimal price, BigDecimal total, OrderStatus estado) {
        InvestmentOrder order = new InvestmentOrder();
        order.setPortfolio(portfolio);
        order.setInstrument(instrument);
        order.setTipo(tipo);
        order.setShares(shares);
        order.setPricePerShare(price);
        order.setTotalAmount(total);
        order.setEstado(estado);
        order.setFechaOrden(LocalDateTime.now());
        return order;
    }

    private PositionResponse buildPositionResponse(Position pos, BigDecimal currentPrice) {
        BigDecimal currentValue = currentPrice.multiply(pos.getShares()).setScale(4, RoundingMode.HALF_UP);
        BigDecimal invested     = pos.getAvgCost().multiply(pos.getShares()).setScale(4, RoundingMode.HALF_UP);
        BigDecimal pnl          = currentValue.subtract(invested);

        PositionResponse pr = new PositionResponse();
        pr.setSymbol(pos.getInstrument().getSymbol());
        pr.setInstrumentName(pos.getInstrument().getName());
        pr.setShares(pos.getShares());
        pr.setAvgCost(pos.getAvgCost());
        pr.setCurrentPrice(currentPrice);
        pr.setCurrentValue(currentValue);
        pr.setInvested(invested);
        pr.setPnl(pnl);
        pr.setPnlPercent(pnlPercent(pnl, invested));
        return pr;
    }

    private BigDecimal pnlPercent(BigDecimal pnl, BigDecimal base) {
        if (base.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
        return pnl.divide(base, 6, RoundingMode.HALF_UP)
                  .multiply(BigDecimal.valueOf(100))
                  .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sum(List<PositionResponse> positions, java.util.function.Function<PositionResponse, BigDecimal> extractor) {
        return positions.stream().map(extractor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String buildReference(String type, String symbol) {
        return "INV-" + type + "-" + symbol + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
