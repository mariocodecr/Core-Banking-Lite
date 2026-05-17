package com.corebanking.modules.transfer.service;

import com.corebanking.modules.transfer.dto.CreateTransferRequest;
import com.corebanking.modules.transfer.dto.TransferResponse;
import com.corebanking.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface TransferService {

    /**
     * Executes an inter-account transfer atomically.
     *
     * Guarantees:
     * - Idempotency: same idempotencyKey returns the existing transfer
     * - Atomicity: debit and credit happen in the same DB transaction
     * - Daily limit: enforced per source account per calendar day
     * - No self-transfer: origin and destination must differ
     */
    TransferResponse transfer(CreateTransferRequest request);

    TransferResponse findById(UUID id);

    PagedResponse<TransferResponse> findAll(Pageable pageable);

    PagedResponse<TransferResponse> findByUserEmail(String email, Pageable pageable);
}
