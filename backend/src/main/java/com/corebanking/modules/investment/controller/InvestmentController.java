package com.corebanking.modules.investment.controller;

import com.corebanking.modules.investment.dto.*;
import com.corebanking.modules.investment.service.InvestmentService;
import com.corebanking.shared.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/v1/investments")
@RequiredArgsConstructor
@Tag(name = "Investments", description = "ETF investment operations — buy, sell, portfolio tracking")
public class InvestmentController {

    private final InvestmentService investmentService;

    @GetMapping("/instruments")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'AUDITOR', 'CLIENT')")
    @Operation(summary = "List all available ETF instruments")
    public ResponseEntity<List<InstrumentResponse>> getInstruments() {
        return ResponseEntity.ok(investmentService.getAvailableInstruments());
    }

    @GetMapping("/instruments/{symbol}/quote")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'AUDITOR', 'CLIENT')")
    @Operation(summary = "Get real-time price quote for an ETF symbol")
    public ResponseEntity<InstrumentResponse> getQuote(@PathVariable String symbol) {
        return ResponseEntity.ok(investmentService.getInstrumentQuote(symbol));
    }

    @GetMapping("/portfolios/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'AUDITOR', 'CLIENT')")
    @Operation(summary = "Get investment portfolio for an account")
    public ResponseEntity<PortfolioResponse> getPortfolio(@PathVariable UUID accountId) {
        return ResponseEntity.ok(investmentService.getPortfolio(accountId));
    }

    @PostMapping("/orders/buy")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'CLIENT')")
    @Operation(summary = "Execute a BUY market order")
    public ResponseEntity<InvestmentOrderResponse> buy(@Valid @RequestBody BuyOrderRequest request) {
        InvestmentOrderResponse order = investmentService.buy(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .replacePath("/api/v1/investments/orders/{id}")
                .buildAndExpand(order.getId()).toUri();
        return ResponseEntity.created(location).body(order);
    }

    @PostMapping("/orders/sell")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'CLIENT')")
    @Operation(summary = "Execute a SELL market order")
    public ResponseEntity<InvestmentOrderResponse> sell(@Valid @RequestBody SellOrderRequest request) {
        return ResponseEntity.ok(investmentService.sell(request));
    }

    @GetMapping("/portfolio")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'AUDITOR', 'CLIENT')")
    @Operation(summary = "Get combined portfolio across all accounts")
    public ResponseEntity<PortfolioResponse> getCombinedPortfolio() {
        return ResponseEntity.ok(investmentService.getCombinedPortfolio());
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'AUDITOR')")
    @Operation(summary = "Get aggregate investment summary across all portfolios")
    public ResponseEntity<InvestmentSummaryResponse> getSummary() {
        return ResponseEntity.ok(investmentService.getInvestmentSummary());
    }

    @GetMapping("/orders/{accountId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'AUDITOR', 'CLIENT')")
    @Operation(summary = "Get order history for an account")
    public ResponseEntity<PagedResponse<InvestmentOrderResponse>> getOrderHistory(
            @PathVariable UUID accountId,
            @PageableDefault(size = 20, sort = "fechaOrden", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(investmentService.getOrderHistory(accountId, pageable));
    }
}
