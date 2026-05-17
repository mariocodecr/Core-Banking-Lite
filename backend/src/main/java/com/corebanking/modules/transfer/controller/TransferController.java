package com.corebanking.modules.transfer.controller;

import com.corebanking.modules.transfer.dto.CreateTransferRequest;
import com.corebanking.modules.transfer.dto.TransferResponse;
import com.corebanking.modules.transfer.service.TransferService;
import com.corebanking.shared.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@Tag(name = "Transfers", description = "Inter-account transfer operations")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/v1/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @Operation(summary = "Execute an inter-account transfer")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'CLIENT')")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody CreateTransferRequest request) {
        TransferResponse response = transferService.transfer(request);
        return ResponseEntity
                .created(URI.create("/api/v1/transfers/" + response.getId()))
                .body(response);
    }

    @Operation(summary = "Get transfer by ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'ADVISOR')")
    public ResponseEntity<TransferResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(transferService.findById(id));
    }

    @Operation(summary = "List all transfers (paginated)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AUDITOR', 'ADVISOR')")
    public ResponseEntity<PagedResponse<TransferResponse>> findAll(
            @PageableDefault(size = 20, sort = "fechaTransferencia") Pageable pageable) {
        return ResponseEntity.ok(transferService.findAll(pageable));
    }

    @Operation(summary = "List transfers for the authenticated user's accounts")
    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'ADVISOR', 'CLIENT', 'AUDITOR')")
    public ResponseEntity<PagedResponse<TransferResponse>> findMine(
            @AuthenticationPrincipal UserDetails principal,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(transferService.findByUserEmail(principal.getUsername(), pageable));
    }
}
