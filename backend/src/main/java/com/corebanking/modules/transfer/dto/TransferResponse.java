package com.corebanking.modules.transfer.dto;

import com.corebanking.modules.transfer.entity.TransferStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransferResponse {

    private UUID id;

    private String numeroCuentaOrigen;
    private String nombreClienteOrigen;

    private String numeroCuentaDestino;
    private String nombreClienteDestino;

    private BigDecimal monto;
    private String moneda;
    private String descripcion;
    private TransferStatus estado;

    private String idempotencyKey;
    private String referencia;
    private String motivoFallo;

    private LocalDateTime fechaTransferencia;
    private LocalDateTime createdAt;
}
