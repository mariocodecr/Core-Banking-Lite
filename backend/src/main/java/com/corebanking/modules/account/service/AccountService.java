package com.corebanking.modules.account.service;

import com.corebanking.modules.account.dto.AccountFilterParams;
import com.corebanking.modules.account.dto.AccountMovementResponse;
import com.corebanking.modules.account.dto.AccountResponse;
import com.corebanking.modules.account.dto.CreateAccountRequest;
import com.corebanking.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AccountService {

    PagedResponse<AccountResponse> findAll(AccountFilterParams filters, Pageable pageable);

    AccountResponse findById(UUID id);

    List<AccountResponse> findByCustomerId(UUID customerId);

    AccountResponse create(CreateAccountRequest request);

    AccountResponse freeze(UUID id);

    AccountResponse unfreeze(UUID id);

    AccountResponse close(UUID id);

    PagedResponse<AccountMovementResponse> getMovements(UUID accountId, Pageable pageable);

    /**
     * Used by TransferService (Phase 5) within its own @Transactional boundary.
     * Validates active account, sufficient balance, and registers a RETIRO movement.
     */
    AccountResponse debit(UUID accountId, BigDecimal amount, String description, String reference);

    /**
     * Used by TransferService (Phase 5) within its own @Transactional boundary.
     * Validates active account and registers a DEPOSITO movement.
     */
    AccountResponse credit(UUID accountId, BigDecimal amount, String description, String reference);
}
