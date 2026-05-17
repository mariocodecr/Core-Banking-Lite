package com.corebanking.modules.account.repository;

import com.corebanking.modules.account.dto.AccountFilterParams;
import com.corebanking.modules.account.entity.Account;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AccountSpecification {

    public static Specification<Account> withFilters(AccountFilterParams params) {
        return Specification
                .where(byCustomerId(params.getCustomerId()))
                .and(byTipo(params.getTipo()))
                .and(byEstado(params.getEstado()))
                .and(byNumeroCuenta(params.getNumeroCuenta()));
    }

    private static Specification<Account> byCustomerId(java.util.UUID customerId) {
        if (customerId == null) return null;
        return (root, query, cb) -> cb.equal(root.get("customer").get("id"), customerId);
    }

    private static Specification<Account> byTipo(com.corebanking.modules.account.entity.AccountType tipo) {
        if (tipo == null) return null;
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    private static Specification<Account> byEstado(com.corebanking.modules.account.entity.AccountStatus estado) {
        if (estado == null) return null;
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }

    private static Specification<Account> byNumeroCuenta(String numeroCuenta) {
        if (numeroCuenta == null || numeroCuenta.isBlank()) return null;
        return (root, query, cb) -> cb.like(root.get("numeroCuenta"), "%" + numeroCuenta + "%");
    }
}
