package com.corebanking.modules.customer.service;

import com.corebanking.modules.customer.dto.CustomerFilterParams;
import com.corebanking.modules.customer.dto.CustomerRequest;
import com.corebanking.modules.customer.dto.CustomerResponse;
import com.corebanking.modules.customer.entity.CustomerStatus;
import com.corebanking.shared.PagedResponse;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {

    PagedResponse<CustomerResponse> findAll(CustomerFilterParams filters, Pageable pageable);

    CustomerResponse findById(UUID id);

    CustomerResponse create(CustomerRequest request);

    CustomerResponse update(UUID id, CustomerRequest request);

    void delete(UUID id);

    CustomerResponse updateStatus(UUID id, CustomerStatus status);
}
