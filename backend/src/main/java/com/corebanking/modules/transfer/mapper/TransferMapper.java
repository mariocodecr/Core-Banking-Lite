package com.corebanking.modules.transfer.mapper;

import com.corebanking.modules.transfer.dto.TransferResponse;
import com.corebanking.modules.transfer.entity.Transfer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferMapper {

    @Mapping(target = "numeroCuentaOrigen",  source = "cuentaOrigen.numeroCuenta")
    @Mapping(target = "nombreClienteOrigen", source = "cuentaOrigen.customer.nombreCompleto")
    @Mapping(target = "numeroCuentaDestino",  source = "cuentaDestino.numeroCuenta")
    @Mapping(target = "nombreClienteDestino", source = "cuentaDestino.customer.nombreCompleto")
    TransferResponse toResponse(Transfer transfer);
}
