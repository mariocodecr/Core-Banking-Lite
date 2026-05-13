package com.corebanking.modules.account.dto;

import com.corebanking.modules.account.entity.AccountType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateAccountRequest {

    @NotNull(message = "El cliente es requerido")
    private UUID customerId;

    @NotNull(message = "El tipo de cuenta es requerido")
    private AccountType tipo;

    @NotBlank(message = "La moneda es requerida")
    @Size(min = 3, max = 3, message = "La moneda debe ser un código ISO de 3 letras")
    @Pattern(regexp = "^(USD|CRC|EUR)$", message = "Moneda no soportada. Use: USD, CRC, EUR")
    private String moneda = "USD";

    @DecimalMin(value = "0.00", message = "El saldo inicial no puede ser negativo")
    @Digits(integer = 13, fraction = 4, message = "Formato de monto inválido")
    private BigDecimal saldoInicial = BigDecimal.ZERO;
}
