package com.corebanking.modules.account.service;

import com.corebanking.exception.BusinessException;
import com.corebanking.exception.ErrorCode;
import com.corebanking.exception.ResourceNotFoundException;
import com.corebanking.modules.account.dto.AccountFilterParams;
import com.corebanking.modules.account.dto.AccountMovementResponse;
import com.corebanking.modules.account.dto.AccountResponse;
import com.corebanking.modules.account.dto.CreateAccountRequest;
import com.corebanking.modules.account.entity.*;
import com.corebanking.modules.account.mapper.AccountMapper;
import com.corebanking.modules.account.repository.AccountMovementRepository;
import com.corebanking.modules.account.repository.AccountRepository;
import com.corebanking.modules.account.repository.AccountSpecification;
import com.corebanking.modules.customer.entity.Customer;
import com.corebanking.modules.customer.repository.CustomerRepository;
import com.corebanking.shared.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository       accountRepository;
    private final AccountMovementRepository movementRepository;
    private final CustomerRepository      customerRepository;
    private final AccountMapper           accountMapper;

    // ─── Queries ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AccountResponse> findAll(AccountFilterParams filters, Pageable pageable) {
        var page = accountRepository.findAll(AccountSpecification.withFilters(filters), pageable);
        return PagedResponse.from(page.map(accountMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public AccountResponse findById(UUID id) {
        return accountMapper.toResponse(findAccountById(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<AccountResponse> findByCustomerId(UUID customerId) {
        return accountMapper.toResponseList(accountRepository.findByCustomerId(customerId));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<AccountMovementResponse> getMovements(UUID accountId, Pageable pageable) {
        findAccountById(accountId); // verify existence
        var page = movementRepository.findByAccountIdOrderByFechaMovimientoDesc(accountId, pageable);
        return PagedResponse.from(page.map(accountMapper::toMovementResponse));
    }

    // ─── Commands ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public AccountResponse create(CreateAccountRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        Account account = new Account();
        account.setCustomer(customer);
        account.setTipo(request.getTipo());
        account.setMoneda(request.getMoneda());
        account.setSaldo(request.getSaldoInicial() != null ? request.getSaldoInicial() : BigDecimal.ZERO);
        account.setEstado(AccountStatus.ACTIVA);
        account.setNumeroCuenta(generateUniqueAccountNumber(request.getTipo()));
        account.setFechaApertura(LocalDate.now());

        Account saved = accountRepository.save(account);

        if (saved.getSaldo().compareTo(BigDecimal.ZERO) > 0) {
            registerMovement(saved, MovementType.DEPOSITO, saved.getSaldo(),
                    BigDecimal.ZERO, "Depósito inicial", null);
        }

        log.info("Account created: numeroCuenta={}, tipo={}, cliente={}",
                saved.getNumeroCuenta(), saved.getTipo(), customer.getNombreCompleto());

        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse freeze(UUID id) {
        Account account = findActiveAccount(id);
        account.setEstado(AccountStatus.CONGELADA);
        log.info("Account frozen: {}", account.getNumeroCuenta());
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountResponse unfreeze(UUID id) {
        Account account = findAccountById(id);
        if (account.getEstado() != AccountStatus.CONGELADA) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "La cuenta no está en estado CONGELADA");
        }
        account.setEstado(AccountStatus.ACTIVA);
        log.info("Account unfrozen: {}", account.getNumeroCuenta());
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountResponse close(UUID id) {
        Account account = findAccountById(id);
        if (account.getEstado() == AccountStatus.CERRADA) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "La cuenta ya está cerrada");
        }
        if (account.getSaldo().compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessException(ErrorCode.BUSINESS_RULE_VIOLATION,
                    "No se puede cerrar una cuenta con saldo. Saldo actual: " + account.getSaldo());
        }
        account.setEstado(AccountStatus.CERRADA);
        account.setFechaCierre(LocalDate.now());
        log.info("Account closed: {}", account.getNumeroCuenta());
        return accountMapper.toResponse(accountRepository.save(account));
    }

    @Override
    @Transactional
    public AccountResponse debit(UUID accountId, BigDecimal amount, String description, String reference) {
        Account account = findActiveAccount(accountId);

        if (account.getSaldo().compareTo(amount) < 0) {
            throw new BusinessException(ErrorCode.INSUFFICIENT_BALANCE,
                    String.format("Saldo insuficiente. Disponible: %s %s, Requerido: %s",
                            account.getMoneda(), account.getSaldo(), amount));
        }

        BigDecimal saldoAnterior = account.getSaldo();
        account.setSaldo(account.getSaldo().subtract(amount));
        Account saved = accountRepository.save(account);

        registerMovement(saved, MovementType.RETIRO, amount, saldoAnterior, description, reference);
        return accountMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AccountResponse credit(UUID accountId, BigDecimal amount, String description, String reference) {
        Account account = findActiveAccount(accountId);

        BigDecimal saldoAnterior = account.getSaldo();
        account.setSaldo(account.getSaldo().add(amount));
        Account saved = accountRepository.save(account);

        registerMovement(saved, MovementType.DEPOSITO, amount, saldoAnterior, description, reference);
        return accountMapper.toResponse(saved);
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private Account findAccountById(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account", "id", id));
    }

    private Account findActiveAccount(UUID id) {
        Account account = findAccountById(id);
        if (account.getEstado() != AccountStatus.ACTIVA) {
            throw new BusinessException(ErrorCode.ACCOUNT_INACTIVE,
                    "La cuenta " + account.getNumeroCuenta() + " no está activa (estado: " + account.getEstado() + ")");
        }
        return account;
    }

    private void registerMovement(Account account, MovementType tipo, BigDecimal monto,
                                   BigDecimal saldoAnterior, String descripcion, String referencia) {
        AccountMovement movement = new AccountMovement();
        movement.setAccount(account);
        movement.setTipo(tipo);
        movement.setMonto(monto);
        movement.setSaldoAnterior(saldoAnterior);
        movement.setSaldoPosterior(account.getSaldo());
        movement.setDescripcion(descripcion);
        movement.setReferencia(referencia);
        movement.setFechaMovimiento(LocalDateTime.now());
        movementRepository.save(movement);
    }

    private String generateUniqueAccountNumber(AccountType tipo) {
        String prefix = switch (tipo) {
            case AHORROS   -> "AHO";
            case CTS       -> "CTS";
            case CORRIENTE -> "COR";
        };
        String numero;
        do {
            numero = "CBL-" + prefix + "-" + String.format("%06d",
                    ThreadLocalRandom.current().nextInt(0, 999_999));
        } while (accountRepository.existsByNumeroCuenta(numero));
        return numero;
    }
}
