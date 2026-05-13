package com.corebanking.modules.transfer.service;

import com.corebanking.exception.BusinessException;
import com.corebanking.exception.ErrorCode;
import com.corebanking.exception.ResourceNotFoundException;
import com.corebanking.modules.account.dto.AccountResponse;
import com.corebanking.modules.account.entity.MovementType;
import com.corebanking.modules.account.repository.AccountRepository;
import com.corebanking.modules.account.service.AccountService;
import com.corebanking.modules.transfer.dto.CreateTransferRequest;
import com.corebanking.modules.transfer.dto.TransferResponse;
import com.corebanking.modules.transfer.entity.Transfer;
import com.corebanking.modules.transfer.entity.TransferStatus;
import com.corebanking.modules.transfer.mapper.TransferMapper;
import com.corebanking.modules.transfer.repository.TransferRepository;
import com.corebanking.shared.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService {

    private final TransferRepository  transferRepository;
    private final TransferMapper      transferMapper;
    private final AccountService      accountService;
    private final AccountRepository   accountRepository;

    @Value("${app.transfers.daily-limit:500000.00}")
    private BigDecimal dailyLimit;

    // ─── Queries ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public TransferResponse findById(UUID id) {
        return transferMapper.toResponse(
                transferRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Transfer", "id", id))
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<TransferResponse> findAll(Pageable pageable) {
        return PagedResponse.from(
                transferRepository.findAllByOrderByFechaTransferenciaDesc(pageable)
                        .map(transferMapper::toResponse)
        );
    }

    // ─── Commands ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public TransferResponse transfer(CreateTransferRequest request) {
        // Idempotency check: return existing transfer for the same key
        return transferRepository.findByIdempotencyKey(request.getIdempotencyKey())
                .map(existing -> {
                    log.info("Duplicate transfer request detected, returning existing transfer: idempotencyKey={}",
                            request.getIdempotencyKey());
                    return transferMapper.toResponse(existing);
                })
                .orElseGet(() -> executeTransfer(request));
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private TransferResponse executeTransfer(CreateTransferRequest request) {
        // 1. Self-transfer guard
        if (request.getCuentaOrigenId().equals(request.getCuentaDestinoId())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "La cuenta de origen y destino no pueden ser la misma");
        }

        // 2. Load accounts (validates existence; debit/credit will validate active status)
        AccountResponse origen  = accountService.findById(request.getCuentaOrigenId());
        AccountResponse destino = accountService.findById(request.getCuentaDestinoId());

        // 3. Currency match guard — cross-currency transfers require a forex module
        if (!origen.getMoneda().equals(destino.getMoneda())) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    String.format("No se permiten transferencias entre monedas distintas (%s → %s). " +
                            "Ambas cuentas deben operar en la misma moneda.",
                            origen.getMoneda(), destino.getMoneda()));
        }

        // 4. Daily limit enforcement
        BigDecimal dailyTotal = transferRepository.sumCompletedTransfersByAccountAndDate(
                request.getCuentaOrigenId(), LocalDate.now().atStartOfDay());
        BigDecimal remaining = dailyLimit.subtract(dailyTotal);
        if (request.getMonto().compareTo(remaining) > 0) {
            throw new BusinessException(ErrorCode.DAILY_LIMIT_EXCEEDED,
                    String.format("Límite diario de transferencias superado. Monto disponible hoy: %s %s",
                            origen.getMoneda(), remaining.toPlainString()));
        }

        // 4. Generate audit reference linking both movements to this transfer
        String referencia = "TRF-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();

        // 5. Atomic debit → credit (both join this @Transactional via REQUIRED propagation)
        String descDebit  = "Transferencia a "    + destino.getNumeroCuenta() + ": " + request.getDescripcion();
        String descCredit = "Transferencia de "   + origen.getNumeroCuenta()  + ": " + request.getDescripcion();

        accountService.debit(request.getCuentaOrigenId(),  request.getMonto(), descDebit,  referencia, MovementType.TRANSFERENCIA_SALIDA);
        accountService.credit(request.getCuentaDestinoId(), request.getMonto(), descCredit, referencia, MovementType.TRANSFERENCIA_ENTRADA);

        // 6. Persist transfer record
        Transfer transfer = new Transfer();
        transfer.setCuentaOrigen(accountRepository.getReferenceById(request.getCuentaOrigenId()));
        transfer.setCuentaDestino(accountRepository.getReferenceById(request.getCuentaDestinoId()));
        transfer.setMonto(request.getMonto());
        transfer.setMoneda(origen.getMoneda());
        transfer.setDescripcion(request.getDescripcion());
        transfer.setEstado(TransferStatus.COMPLETADA);
        transfer.setIdempotencyKey(request.getIdempotencyKey());
        transfer.setReferencia(referencia);
        transfer.setFechaTransferencia(LocalDateTime.now());

        Transfer saved = transferRepository.save(transfer);

        log.info("Transfer completed: referencia={}, origen={}, destino={}, monto={} {}",
                referencia, origen.getNumeroCuenta(), destino.getNumeroCuenta(),
                request.getMonto(), origen.getMoneda());

        return transferMapper.toResponse(saved);
    }
}
