package com.corebanking.modules.customer.service;

import com.corebanking.exception.BusinessException;
import com.corebanking.exception.ErrorCode;
import com.corebanking.exception.ResourceNotFoundException;
import com.corebanking.modules.customer.dto.CustomerFilterParams;
import com.corebanking.modules.customer.dto.CustomerRequest;
import com.corebanking.modules.customer.dto.CustomerResponse;
import com.corebanking.modules.customer.entity.Customer;
import com.corebanking.modules.customer.entity.CustomerStatus;
import com.corebanking.modules.customer.entity.DocumentType;
import com.corebanking.modules.customer.mapper.CustomerMapper;
import com.corebanking.modules.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerServiceImpl unit tests")
class CustomerServiceImplTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private CustomerMapper customerMapper;

    @InjectMocks private CustomerServiceImpl customerService;

    private Customer customer;
    private CustomerResponse customerResponse;
    private CustomerRequest customerRequest;
    private final UUID customerId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(customerId);
        customer.setTipoDocumento(DocumentType.DNI);
        customer.setNumeroDocumento("12345678");
        customer.setNombres("Juan");
        customer.setApellidos("Pérez");
        customer.setEmail("juan.perez@test.com");
        customer.setEstado(CustomerStatus.ACTIVO);

        customerResponse = new CustomerResponse();
        customerResponse.setId(customerId);
        customerResponse.setNombres("Juan");
        customerResponse.setApellidos("Pérez");
        customerResponse.setEstado(CustomerStatus.ACTIVO);

        customerRequest = new CustomerRequest();
        customerRequest.setTipoDocumento(DocumentType.DNI);
        customerRequest.setNumeroDocumento("12345678");
        customerRequest.setNombres("Juan");
        customerRequest.setApellidos("Pérez");
        customerRequest.setEmail("juan.perez@test.com");
        customerRequest.setFechaNacimiento(LocalDate.of(1990, 1, 1));
    }

    @Test
    @DisplayName("findAll — should return paged response")
    void findAll_returnsPagedResponse() {
        Page<Customer> page = new PageImpl<>(List.of(customer));
        when(customerRepository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        var result = customerService.findAll(new CustomerFilterParams(), PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    @DisplayName("findById — existing ID should return customer")
    void findById_existingId_returnsCustomer() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        CustomerResponse result = customerService.findById(customerId);

        assertThat(result.getId()).isEqualTo(customerId);
    }

    @Test
    @DisplayName("findById — unknown ID should throw ResourceNotFoundException")
    void findById_unknownId_throwsException() {
        when(customerRepository.findById(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.findById(UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("create — unique document should create customer")
    void create_uniqueDocument_createsCustomer() {
        when(customerRepository.existsByNumeroDocumento(customerRequest.getNumeroDocumento())).thenReturn(false);
        when(customerRepository.existsByEmailIgnoreCase(customerRequest.getEmail())).thenReturn(false);
        when(customerMapper.toEntity(customerRequest)).thenReturn(customer);
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        CustomerResponse result = customerService.create(customerRequest);

        assertThat(result).isNotNull();
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("create — duplicate document should throw DUPLICATE_RESOURCE")
    void create_duplicateDocument_throwsBusinessException() {
        when(customerRepository.existsByNumeroDocumento(customerRequest.getNumeroDocumento())).thenReturn(true);

        assertThatThrownBy(() -> customerService.create(customerRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(ex -> assertThat(((BusinessException) ex).getErrorCode())
                        .isEqualTo(ErrorCode.DUPLICATE_RESOURCE));

        verify(customerRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete — should set deleted=true and save")
    void delete_existingCustomer_softDeletes() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        customerService.delete(customerId);

        assertThat(customer.isDeleted()).isTrue();
        assertThat(customer.getDeletedAt()).isNotNull();
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("updateStatus — should change customer status")
    void updateStatus_changesStatus() {
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(customer)).thenReturn(customer);
        when(customerMapper.toResponse(customer)).thenReturn(customerResponse);

        customerService.updateStatus(customerId, CustomerStatus.INACTIVO);

        assertThat(customer.getEstado()).isEqualTo(CustomerStatus.INACTIVO);
    }
}
