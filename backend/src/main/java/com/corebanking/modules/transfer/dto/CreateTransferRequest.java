package com.corebanking.modules.transfer.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class CreateTransferRequest {

    @NotNull(message = "La cuenta de origen es requerida")
    private UUID cuentaOrigenId;

    @NotNull(message = "La cuenta de destino es requerida")
    private UUID cuentaDestinoId;

    @NotNull(message = "El monto es requerido")
    @DecimalMin(value = "0.01", message = "El monto mínimo de transferencia es 0.01")
    @Digits(integer = 15, fraction = 4, message = "Formato de monto inválido")
    private BigDecimal monto;

    @NotBlank(message = "La descripción es requerida")
    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String descripcion;

    /**
     * Client-generated unique key to prevent duplicate transfers on retries.
     * Recommended: UUID v4 generated on the frontend before submitting.
     */
    @NotBlank(message = "El idempotency key es requerido")
    @Size(max = 100, message = "El idempotency key no puede superar 100 caracteres")
    private String idempotencyKey;
}
