package com.corebanking.modules.account.mapper;

import com.corebanking.modules.account.dto.AccountMovementResponse;
import com.corebanking.modules.account.dto.AccountResponse;
import com.corebanking.modules.account.entity.Account;
import com.corebanking.modules.account.entity.AccountMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * customer.id   → customerId
     * customer.getNombreCompleto() → nombreCliente
     *
     * MapStruct resolves getter methods on source objects — no need for intermediate DTO.
     */
    @Mapping(target = "customerId",    source = "customer.id")
    @Mapping(target = "nombreCliente", source = "customer.nombreCompleto")
    AccountResponse toResponse(Account account);

    List<AccountResponse> toResponseList(List<Account> accounts);

    AccountMovementResponse toMovementResponse(AccountMovement movement);
}
