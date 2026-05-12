package com.corebanking.modules.customer.service;

import com.corebanking.exception.BusinessException;
import com.corebanking.exception.ErrorCode;
import com.corebanking.exception.ResourceNotFoundException;
import com.corebanking.modules.customer.dto.CustomerFilterParams;
import com.corebanking.modules.customer.dto.CustomerRequest;
import com.corebanking.modules.customer.dto.CustomerResponse;
import com.corebanking.modules.customer.entity.Customer;
import com.corebanking.modules.customer.entity.CustomerStatus;
import com.corebanking.modules.customer.mapper.CustomerMapper;
import com.corebanking.modules.customer.repository.CustomerRepository;
import com.corebanking.modules.customer.repository.CustomerSpecification;
import com.corebanking.shared.PagedResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<CustomerResponse> findAll(CustomerFilterParams filters, Pageable pageable) {
        var spec = CustomerSpecification.withFilters(filters);
        var page = customerRepository.findAll(spec, pageable);
        return PagedResponse.from(page.map(customerMapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        return customerMapper.toResponse(findCustomerById(id));
    }

    @Override
    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        validateUniqueDocument(request.getNumeroDocumento(), null);
        validateUniqueEmail(request.getEmail(), null);

        Customer customer = customerMapper.toEntity(request);
        Customer saved = customerRepository.save(customer);

        log.info("Customer created: id={}, document={}", saved.getId(), saved.getNumeroDocumento());
        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = findCustomerById(id);

        validateUniqueDocument(request.getNumeroDocumento(), customer.getNumeroDocumento());
        validateUniqueEmail(request.getEmail(), customer.getEmail());

        customerMapper.updateEntity(customer, request);
        Customer saved = customerRepository.save(customer);

        log.info("Customer updated: id={}", saved.getId());
        return customerMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Customer customer = findCustomerById(id);
        customer.setDeleted(true);
        customer.setDeletedAt(LocalDateTime.now());
        customerRepository.save(customer);
        log.info("Customer soft-deleted: id={}", id);
    }

    @Override
    @Transactional
    public CustomerResponse updateStatus(UUID id, CustomerStatus status) {
        Customer customer = findCustomerById(id);
        customer.setEstado(status);
        return customerMapper.toResponse(customerRepository.save(customer));
    }

    // ─── Private helpers ─────────────────────────────────────────────────────

    private Customer findCustomerById(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
    }

    private void validateUniqueDocument(String newDocument, String currentDocument) {
        if (newDocument.equals(currentDocument)) return;
        if (customerRepository.existsByNumeroDocumento(newDocument)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Ya existe un cliente con el documento: " + newDocument);
        }
    }

    private void validateUniqueEmail(String newEmail, String currentEmail) {
        if (newEmail == null || newEmail.isBlank()) return;
        if (newEmail.equalsIgnoreCase(currentEmail)) return;
        if (customerRepository.existsByEmailIgnoreCase(newEmail)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE,
                    "Ya existe un cliente con el email: " + newEmail);
        }
    }
}
