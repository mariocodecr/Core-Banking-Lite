package com.corebanking.modules.account.service;

import com.corebanking.exception.BusinessException;
import com.corebanking.exception.ErrorCode;
import com.corebanking.modules.account.dto.AccountResponse;
import com.corebanking.modules.account.dto.CreateAccountRequest;
import com.corebanking.modules.account.entity.*;
import com.corebanking.modules.account.mapper.AccountMapper;
import com.corebanking.modules.account.repository.AccountMovementRepository;
import com.corebanking.modules.account.repository.AccountRepository;
import com.corebanking.modules.customer.entity.Customer;
import com.corebanking.modules.customer.entity.CustomerStatus;
import com.corebanking.modules.customer.entity.DocumentType;
import com.corebanking.modules.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AccountServiceImpl unit tests")
class AccountServiceImplTest {

    @Mock private AccountRepository         accountRepository;
    @Mock private AccountMovementRepository movementRepository;
    @Mock private CustomerRepository        customerRepository;
    @Mock private AccountMapper             accountMapper;

    @InjectMocks private AccountServiceImpl accountService;

    private Account         account;
    private Customer        customer;
    private AccountResponse accountResponse;

    private final UUID accountId  = UUID.randomUUID();
    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setNombres("Juan");
        customer.setApellidos("Pérez");
        customer.setEmail("juan@test.com");
        customer.setTipoDocumento(DocumentType.DNI);
        customer.setNumeroDocumento("12345678");
        customer.setEstado(CustomerStatus.ACTIVO);

        account = new Account();
        account.setId(accountId);
        account.setTipo(AccountType.AHORROS);
        account.setEstado(AccountStatus.ACTIVA);
        account.setSaldo(new BigDecimal("1000.00"));
        account.setMoneda("PEN");
        account.setNumeroCuenta("CBL-AHO-001234");
        account.setFechaApertura(LocalDate.now());
        account.setCustomer(customer);

        accountResponse = new AccountResponse();
        accountResponse.setId(accountId);
        accountResponse.setSaldo(new BigDecimal("1000.00"));
        accountResponse.setEstado(AccountStatus.ACTIVA);
    }

    // ─── create ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("create — zero initial balance should NOT register a movement")
    void create_withZeroBalance_noMovementRegistered() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(customerId);
        request.setTipo(AccountType.AHORROS);
        request.setMoneda("PEN");
        request.setSaldoInicial(BigDecimal.ZERO);

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(accountRepository.existsByNumeroCuenta(any())).thenReturn(false);
        when(accountRepository.save(any())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        account.setSaldo(BigDecimal.ZERO);
        accountService.create(request);

        verify(accountRepository).save(any(Account.class));
        verify(movementRepository, never()).save(any());
    }

    @Test
    @DisplayName("create — positive initial balance should register DEPOSITO movement")
    void create_withPositiveBalance_registersMovement() {
        CreateAccountRequest request = new CreateAccountRequest();
        request.setCustomerId(customerId);
        request.setTipo(AccountType.AHORROS);
        request.setMoneda("PEN");
        request.setSaldoInicial(new BigDecimal("500.00"));

        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(accountRepository.existsByNumeroCuenta(any())).thenReturn(false);
        when(accountRepository.save(any())).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        accountService.create(request);

        verify(movementRepository).save(any(AccountMovement.class));
    }

    // ─── freeze / unfreeze ───────────────────────────────────────────────────

    @Test
    @DisplayName("freeze — active account should become CONGELADA")
    void freeze_activeAccount_becomesCongelada() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        accountService.freeze(accountId);

        assertThat(account.getEstado()).isEqualTo(AccountStatus.CONGELADA);
    }

    @Test
    @DisplayName("freeze — non-active account should throw ACCOUNT_INACTIVE")
    void freeze_frozenAccount_throwsException() {
        account.setEstado(AccountStatus.CONGELADA);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.freeze(accountId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCOUNT_INACTIVE));
    }

    // ─── close ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("close — account with zero balance should close and set fechaCierre")
    void close_zeroBalance_closesAccount() {
        account.setSaldo(BigDecimal.ZERO);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        accountService.close(accountId);

        assertThat(account.getEstado()).isEqualTo(AccountStatus.CERRADA);
        assertThat(account.getFechaCierre()).isNotNull();
    }

    @Test
    @DisplayName("close — account with remaining balance should throw BUSINESS_RULE_VIOLATION")
    void close_withBalance_throwsException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.close(accountId))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.BUSINESS_RULE_VIOLATION));
    }

    // ─── debit / credit ──────────────────────────────────────────────────────

    @Test
    @DisplayName("debit — sufficient balance should subtract amount and register movement")
    void debit_sufficientBalance_subtractsAndRegisters() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        accountService.debit(accountId, new BigDecimal("300.00"), "Pago servicio", null);

        assertThat(account.getSaldo()).isEqualByComparingTo("700.00");
        verify(movementRepository).save(any(AccountMovement.class));
    }

    @Test
    @DisplayName("debit — insufficient balance should throw INSUFFICIENT_BALANCE")
    void debit_insufficientBalance_throwsException() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.debit(accountId, new BigDecimal("9999.00"), "Pago", null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.INSUFFICIENT_BALANCE));

        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("credit — should add amount and register movement")
    void credit_activeAccount_addsAmountAndRegisters() {
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
        when(accountRepository.save(account)).thenReturn(account);
        when(accountMapper.toResponse(account)).thenReturn(accountResponse);

        accountService.credit(accountId, new BigDecimal("250.00"), "Depósito", null);

        assertThat(account.getSaldo()).isEqualByComparingTo("1250.00");
        verify(movementRepository).save(any(AccountMovement.class));
    }

    @Test
    @DisplayName("debit — frozen account should throw ACCOUNT_INACTIVE")
    void debit_frozenAccount_throwsException() {
        account.setEstado(AccountStatus.CONGELADA);
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        assertThatThrownBy(() -> accountService.debit(accountId, new BigDecimal("100.00"), "Pago", null))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.ACCOUNT_INACTIVE));
    }
}
