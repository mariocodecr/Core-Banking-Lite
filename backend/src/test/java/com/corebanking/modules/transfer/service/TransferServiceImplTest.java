package com.corebanking.modules.transfer.service;

import com.corebanking.exception.BusinessException;
import com.corebanking.exception.ErrorCode;
import com.corebanking.modules.account.dto.AccountResponse;
import com.corebanking.modules.account.entity.AccountStatus;
import com.corebanking.modules.account.entity.AccountType;
import com.corebanking.modules.account.entity.MovementType;
import com.corebanking.modules.account.repository.AccountRepository;
import com.corebanking.modules.account.service.AccountService;
import com.corebanking.modules.transfer.dto.CreateTransferRequest;
import com.corebanking.modules.transfer.dto.TransferResponse;
import com.corebanking.modules.transfer.entity.Transfer;
import com.corebanking.modules.transfer.entity.TransferStatus;
import com.corebanking.modules.transfer.mapper.TransferMapper;
import com.corebanking.modules.transfer.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransferServiceImpl unit tests")
class TransferServiceImplTest {

    @Mock private TransferRepository  transferRepository;
    @Mock private TransferMapper      transferMapper;
    @Mock private AccountService      accountService;
    @Mock private AccountRepository   accountRepository;

    @InjectMocks private TransferServiceImpl transferService;

    private final UUID origenId  = UUID.randomUUID();
    private final UUID destinoId = UUID.randomUUID();

    private AccountResponse origenResponse;
    private AccountResponse destinoResponse;
    private CreateTransferRequest request;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(transferService, "dailyLimit", new BigDecimal("50000.00"));

        origenResponse = new AccountResponse();
        origenResponse.setId(origenId);
        origenResponse.setNumeroCuenta("CBL-AHO-001");
        origenResponse.setSaldo(new BigDecimal("5000.00"));
        origenResponse.setMoneda("PEN");
        origenResponse.setEstado(AccountStatus.ACTIVA);
        origenResponse.setTipo(AccountType.AHORROS);

        destinoResponse = new AccountResponse();
        destinoResponse.setId(destinoId);
        destinoResponse.setNumeroCuenta("CBL-AHO-002");
        destinoResponse.setSaldo(new BigDecimal("1000.00"));
        destinoResponse.setMoneda("PEN");
        destinoResponse.setEstado(AccountStatus.ACTIVA);
        destinoResponse.setTipo(AccountType.AHORROS);

        request = new CreateTransferRequest();
        request.setCuentaOrigenId(origenId);
        request.setCuentaDestinoId(destinoId);
        request.setMonto(new BigDecimal("500.00"));
        request.setDescripcion("Pago alquiler");
        request.setIdempotencyKey("key-" + UUID.randomUUID());
    }

    // ─── Idempotency ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("transfer — duplicate idempotency key returns existing transfer without executing again")
    void transfer_duplicateKey_returnsExisting() {
        Transfer existing = new Transfer();
        existing.setEstado(TransferStatus.COMPLETADA);

        TransferResponse expectedResponse = new TransferResponse();
        expectedResponse.setEstado(TransferStatus.COMPLETADA);

        when(transferRepository.findByIdempotencyKey(request.getIdempotencyKey()))
                .thenReturn(Optional.of(existing));
        when(transferMapper.toResponse(existing)).thenReturn(expectedResponse);

        TransferResponse result = transferService.transfer(request);

        assertThat(result.getEstado()).isEqualTo(TransferStatus.COMPLETADA);
        verify(accountService, never()).debit(any(), any(), any(), any(), any());
        verify(accountService, never()).credit(any(), any(), any(), any(), any());
    }

    // ─── Self-transfer guard ──────────────────────────────────────────────────

    @Test
    @DisplayName("transfer — same origin and destination throws BUSINESS_RULE_VIOLATION")
    void transfer_sameAccount_throwsException() {
        request.setCuentaDestinoId(origenId); // same as origin

        when(transferRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BUSINESS_RULE_VIOLATION));

        verify(accountService, never()).debit(any(), any(), any(), any(), any());
    }

    // ─── Daily limit ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("transfer — exceeding daily limit throws DAILY_LIMIT_EXCEEDED")
    void transfer_exceedsDailyLimit_throwsException() {
        when(transferRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountService.findById(origenId)).thenReturn(origenResponse);
        when(accountService.findById(destinoId)).thenReturn(destinoResponse);
        // Already transferred 49800, new transfer for 500 = 50300 > 50000
        when(transferRepository.sumCompletedTransfersByAccountAndDate(eq(origenId), any()))
                .thenReturn(new BigDecimal("49800.00"));

        assertThatThrownBy(() -> transferService.transfer(request))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DAILY_LIMIT_EXCEEDED));
    }

    // ─── Happy path ───────────────────────────────────────────────────────────

    @Test
    @DisplayName("transfer — valid request debits origin and credits destination")
    void transfer_valid_executesDebitAndCredit() {
        Transfer savedTransfer = buildCompletedTransfer();
        TransferResponse expectedResponse = new TransferResponse();
        expectedResponse.setEstado(TransferStatus.COMPLETADA);

        when(transferRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountService.findById(origenId)).thenReturn(origenResponse);
        when(accountService.findById(destinoId)).thenReturn(destinoResponse);
        when(transferRepository.sumCompletedTransfersByAccountAndDate(any(), any()))
                .thenReturn(BigDecimal.ZERO);
        when(accountRepository.getReferenceById(any())).thenReturn(null); // proxy, not needed in unit test
        when(accountService.debit(any(), any(), any(), any(), eq(MovementType.TRANSFERENCIA_SALIDA)))
                .thenReturn(origenResponse);
        when(accountService.credit(any(), any(), any(), any(), eq(MovementType.TRANSFERENCIA_ENTRADA)))
                .thenReturn(destinoResponse);
        when(transferRepository.save(any(Transfer.class))).thenReturn(savedTransfer);
        when(transferMapper.toResponse(savedTransfer)).thenReturn(expectedResponse);

        TransferResponse result = transferService.transfer(request);

        assertThat(result.getEstado()).isEqualTo(TransferStatus.COMPLETADA);
        verify(accountService).debit(eq(origenId), eq(request.getMonto()), anyString(), anyString(), eq(MovementType.TRANSFERENCIA_SALIDA));
        verify(accountService).credit(eq(destinoId), eq(request.getMonto()), anyString(), anyString(), eq(MovementType.TRANSFERENCIA_ENTRADA));
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    @DisplayName("transfer — transfer within daily limit (exactly at boundary) succeeds")
    void transfer_exactlyAtDailyLimit_succeeds() {
        // 49500 already done, transferring 500 = exactly 50000
        Transfer savedTransfer = buildCompletedTransfer();
        TransferResponse expectedResponse = new TransferResponse();

        when(transferRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(accountService.findById(origenId)).thenReturn(origenResponse);
        when(accountService.findById(destinoId)).thenReturn(destinoResponse);
        when(transferRepository.sumCompletedTransfersByAccountAndDate(any(), any()))
                .thenReturn(new BigDecimal("49500.00"));
        when(accountRepository.getReferenceById(any())).thenReturn(null);
        when(accountService.debit(any(), any(), any(), any(), any())).thenReturn(origenResponse);
        when(accountService.credit(any(), any(), any(), any(), any())).thenReturn(destinoResponse);
        when(transferRepository.save(any())).thenReturn(savedTransfer);
        when(transferMapper.toResponse(any())).thenReturn(expectedResponse);

        // Should not throw
        transferService.transfer(request);

        verify(transferRepository).save(any(Transfer.class));
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private Transfer buildCompletedTransfer() {
        Transfer t = new Transfer();
        t.setEstado(TransferStatus.COMPLETADA);
        t.setMonto(request.getMonto());
        t.setMoneda("PEN");
        t.setDescripcion(request.getDescripcion());
        t.setIdempotencyKey(request.getIdempotencyKey());
        t.setReferencia("TRF-ABCDEF123456");
        t.setFechaTransferencia(LocalDateTime.now());
        return t;
    }
}
