package com.corebanking.modules.customer.dto;

import com.corebanking.modules.customer.entity.CustomerStatus;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerFilterParams {
    private String nombre;
    private String numeroDocumento;
    private String email;
    private CustomerStatus estado;
}
