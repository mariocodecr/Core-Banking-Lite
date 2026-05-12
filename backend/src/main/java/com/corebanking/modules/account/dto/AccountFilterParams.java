package com.corebanking.modules.account.dto;

import com.corebanking.modules.account.entity.AccountStatus;
import com.corebanking.modules.account.entity.AccountType;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AccountFilterParams {
    private UUID customerId;
    private AccountType tipo;
    private AccountStatus estado;
    private String numeroCuenta;
}
