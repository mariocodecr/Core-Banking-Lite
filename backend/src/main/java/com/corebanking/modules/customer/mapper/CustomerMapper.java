package com.corebanking.modules.customer.mapper;

import com.corebanking.modules.customer.dto.CustomerRequest;
import com.corebanking.modules.customer.dto.CustomerResponse;
import com.corebanking.modules.customer.entity.Customer;
import com.corebanking.modules.customer.entity.CustomerStatus;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    CustomerResponse toResponse(Customer customer);

    List<CustomerResponse> toResponseList(List<Customer> customers);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "estado",     expression = "java(CustomerStatus.ACTIVO)")
    @Mapping(target = "deleted",    ignore = true)
    @Mapping(target = "deletedAt",  ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "updatedBy",  ignore = true)
    Customer toEntity(CustomerRequest request);

    @Mapping(target = "id",         ignore = true)
    @Mapping(target = "estado",     ignore = true)
    @Mapping(target = "deleted",    ignore = true)
    @Mapping(target = "deletedAt",  ignore = true)
    @Mapping(target = "createdAt",  ignore = true)
    @Mapping(target = "updatedAt",  ignore = true)
    @Mapping(target = "createdBy",  ignore = true)
    @Mapping(target = "updatedBy",  ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Customer customer, CustomerRequest request);
}
