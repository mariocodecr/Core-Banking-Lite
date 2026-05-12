package com.corebanking.modules.customer.repository;

import com.corebanking.modules.customer.dto.CustomerFilterParams;
import com.corebanking.modules.customer.entity.Customer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * JPA Specifications for dynamic Customer queries.
 * Each predicate is null-safe — returning null skips the clause entirely.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CustomerSpecification {

    public static Specification<Customer> withFilters(CustomerFilterParams params) {
        return Specification
                .where(byNombre(params.getNombre()))
                .and(byNumeroDocumento(params.getNumeroDocumento()))
                .and(byEmail(params.getEmail()))
                .and(byEstado(params.getEstado()));
    }

    private static Specification<Customer> byNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) return null;
        String pattern = "%" + nombre.toLowerCase() + "%";
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("nombres")), pattern),
                cb.like(cb.lower(root.get("apellidos")), pattern)
        );
    }

    private static Specification<Customer> byNumeroDocumento(String numero) {
        if (numero == null || numero.isBlank()) return null;
        return (root, query, cb) -> cb.like(root.get("numeroDocumento"), "%" + numero + "%");
    }

    private static Specification<Customer> byEmail(String email) {
        if (email == null || email.isBlank()) return null;
        return (root, query, cb) -> cb.like(cb.lower(root.get("email")), "%" + email.toLowerCase() + "%");
    }

    private static Specification<Customer> byEstado(com.corebanking.modules.customer.entity.CustomerStatus estado) {
        if (estado == null) return null;
        return (root, query, cb) -> cb.equal(root.get("estado"), estado);
    }
}
