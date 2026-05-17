package com.corebanking.modules.customer.dto;

import com.corebanking.modules.customer.entity.DocumentType;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class CustomerRequest {

    @NotNull(message = "El tipo de documento es requerido")
    private DocumentType tipoDocumento;

    @NotBlank(message = "El número de documento es requerido")
    @Size(min = 8, max = 12, message = "El número de documento debe tener entre 8 y 12 dígitos")
    @Pattern(regexp = "^[0-9]+$", message = "El número de documento solo debe contener dígitos")
    private String numeroDocumento;

    @NotBlank(message = "Los nombres son requeridos")
    @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
    private String nombres;

    @NotBlank(message = "Los apellidos son requeridos")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    private String apellidos;

    @Email(message = "El formato del email es inválido")
    @Size(max = 150)
    private String email;

    @Pattern(regexp = "^[+]?[0-9]{9,15}$", message = "El formato del teléfono es inválido")
    private String telefono;

    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;
}
