package com.corebanking.modules.account.dto;

import com.corebanking.modules.account.entity.MovementType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class AccountMovementResponse {
    private UUID id;
    private MovementType tipo;
    private BigDecimal monto;
    private BigDecimal saldoAnterior;
    private BigDecimal saldoPosterior;
    private String descripcion;
    private String referencia;
    private LocalDateTime fechaMovimiento;
}
