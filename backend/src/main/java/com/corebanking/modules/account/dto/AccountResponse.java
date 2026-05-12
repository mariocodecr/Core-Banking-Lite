package com.corebanking.modules.account.dto;

import com.corebanking.modules.account.entity.AccountStatus;
import com.corebanking.modules.account.entity.AccountType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AccountResponse {
    private UUID id;
    private String numeroCuenta;
    private UUID customerId;
    private String nombreCliente;
    private AccountType tipo;
    private AccountStatus estado;
    private BigDecimal saldo;
    private String moneda;
    private LocalDate fechaApertura;
    private LocalDate fechaCierre;
    private LocalDateTime createdAt;
    private Long version;
}
