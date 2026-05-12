package com.corebanking.modules.account.controller;

import com.corebanking.modules.account.dto.AccountFilterParams;
import com.corebanking.modules.account.dto.AccountMovementResponse;
import com.corebanking.modules.account.dto.AccountResponse;
import com.corebanking.modules.account.dto.CreateAccountRequest;
import com.corebanking.modules.account.service.AccountService;
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
@RequestMapping("/v1/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Bank account management and financial operations")
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'AUDITOR')")
    @Operation(summary = "List accounts with optional filters")
    public ResponseEntity<PagedResponse<AccountResponse>> findAll(
            AccountFilterParams filters,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(accountService.findAll(filters, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'AUDITOR', 'CLIENT')")
    @Operation(summary = "Get account by ID")
    public ResponseEntity<AccountResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.findById(id));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'AUDITOR')")
    @Operation(summary = "Get all accounts belonging to a customer")
    public ResponseEntity<List<AccountResponse>> findByCustomerId(@PathVariable UUID customerId) {
        return ResponseEntity.ok(accountService.findByCustomerId(customerId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR')")
    @Operation(summary = "Open a new bank account")
    public ResponseEntity<AccountResponse> create(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse created = accountService.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/{id}/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'AUDITOR', 'CLIENT')")
    @Operation(summary = "Get movement history for an account")
    public ResponseEntity<PagedResponse<AccountMovementResponse>> getMovements(
            @PathVariable UUID id,
            @PageableDefault(size = 20, sort = "fechaMovimiento", direction = Sort.Direction.DESC) Pageable pageable) {

        return ResponseEntity.ok(accountService.getMovements(id, pageable));
    }

    @PatchMapping("/{id}/freeze")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR')")
    @Operation(summary = "Freeze an active account")
    public ResponseEntity<AccountResponse> freeze(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.freeze(id));
    }

    @PatchMapping("/{id}/unfreeze")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR')")
    @Operation(summary = "Unfreeze a frozen account")
    public ResponseEntity<AccountResponse> unfreeze(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.unfreeze(id));
    }

    @PatchMapping("/{id}/close")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Close an account (requires zero balance)")
    public ResponseEntity<AccountResponse> close(@PathVariable UUID id) {
        return ResponseEntity.ok(accountService.close(id));
    }
}
