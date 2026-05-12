package com.corebanking.modules.account.service;

import com.corebanking.modules.account.dto.AccountFilterParams;
import com.corebanking.modules.account.dto.AccountMovementResponse;
import com.corebanking.modules.account.dto.AccountResponse;
import com.corebanking.modules.account.dto.CreateAccountRequest;
import com.corebanking.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import com.corebanking.modules.account.entity.MovementType;

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
     * Used by TransferService within its own @Transactional boundary.
     * Validates active account, sufficient balance, and registers a movement.
     *
     * @param movementType RETIRO for standalone withdrawals, TRANSFERENCIA_SALIDA for transfers
     */
    AccountResponse debit(UUID accountId, BigDecimal amount, String description, String reference, MovementType movementType);

    /**
     * Used by TransferService within its own @Transactional boundary.
     * Validates active account and registers a movement.
     *
     * @param movementType DEPOSITO for standalone deposits, TRANSFERENCIA_ENTRADA for transfers
     */
    AccountResponse credit(UUID accountId, BigDecimal amount, String description, String reference, MovementType movementType);
}
