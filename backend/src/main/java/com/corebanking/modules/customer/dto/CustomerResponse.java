package com.corebanking.modules.customer.dto;

import com.corebanking.modules.customer.entity.CustomerStatus;
import com.corebanking.modules.customer.entity.DocumentType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CustomerResponse {
    private UUID id;
    private DocumentType tipoDocumento;
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String nombreCompleto;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private CustomerStatus estado;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
}
