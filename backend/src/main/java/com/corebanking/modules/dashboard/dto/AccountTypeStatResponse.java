package com.corebanking.modules.dashboard.dto;

import com.corebanking.modules.account.entity.AccountType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccountTypeStatResponse {
    private AccountType tipo;
    private long count;
    private BigDecimal totalBalance;
}
