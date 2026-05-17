package com.corebanking.modules.dashboard.controller;

import com.corebanking.modules.dashboard.dto.AccountTypeStatResponse;
import com.corebanking.modules.dashboard.dto.DailyTransferStatResponse;
import com.corebanking.modules.dashboard.dto.DashboardSummaryResponse;
import com.corebanking.modules.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Dashboard", description = "Financial KPIs and analytics")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "Get financial summary KPIs")
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'ADVISOR')")
    public ResponseEntity<DashboardSummaryResponse> getSummary() {
        return ResponseEntity.ok(dashboardService.getSummary());
    }

    @Operation(summary = "Get account distribution by type")
    @GetMapping("/accounts/by-type")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'ADVISOR')")
    public ResponseEntity<List<AccountTypeStatResponse>> getAccountTypeStats() {
        return ResponseEntity.ok(dashboardService.getAccountTypeStats());
    }

    @Operation(summary = "Get daily transfer volume for the last N days (default 30)")
    @GetMapping("/transfers/daily")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'ADVISOR')")
    public ResponseEntity<List<DailyTransferStatResponse>> getDailyTransferStats(
            @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(dashboardService.getDailyTransferStats(days));
    }
}
