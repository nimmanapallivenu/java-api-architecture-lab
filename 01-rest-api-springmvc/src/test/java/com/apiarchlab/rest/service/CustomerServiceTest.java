package com.apiarchlab.rest.service;

import com.apiarchlab.rest.dto.CreateCustomerRequest;
import com.apiarchlab.rest.dto.CustomerDto;
import com.apiarchlab.rest.entity.CustomerEntity;
import com.apiarchlab.rest.exception.BusinessException;
import com.apiarchlab.rest.exception.ResourceNotFoundException;
import com.apiarchlab.rest.mapper.EntityDtoMapper;
import com.apiarchlab.rest.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Display;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Display("Customer Service Tests")
class CustomerServiceTest {
    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private EntityDtoMapper mapper;

    @InjectMocks
    private CustomerService customerService;

    private CustomerEntity customerEntity;
    private CustomerDto customerDto;
    private CreateCustomerRequest createRequest;

    @BeforeEach
    void setUp() {
        customerEntity = CustomerEntity.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("9876543210")
                .address("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .country("USA")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        customerDto = CustomerDto.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("9876543210")
                .address("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .country("USA")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateCustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .phone("9876543210")
                .address("123 Main St")
                .city("Springfield")
                .state("IL")
                .zipCode("62701")
                .country("USA")
                .build();
    }

    @Test
    @Display("Should retrieve customer by ID successfully")
    void testGetCustomerById() {
        // Arrange
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customerEntity));
        when(mapper.customerEntityToDto(customerEntity)).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.getCustomerById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("john@example.com", result.getEmail());
        verify(customerRepository, times(1)).findById(1L);
    }

    @Test
    @Display("Should throw exception when customer not found")
    void testGetCustomerByIdNotFound() {
        // Arrange
        when(customerRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            customerService.getCustomerById(999L);
        });
        verify(customerRepository, times(1)).findById(999L);
    }

    @Test
    @Display("Should create customer successfully")
    void testCreateCustomer() {
        // Arrange
        when(customerRepository.existsByEmail(anyString())).thenReturn(false);
        when(customerRepository.save(any(CustomerEntity.class))).thenReturn(customerEntity);
        when(mapper.customerEntityToDto(customerEntity)).thenReturn(customerDto);

        // Act
        CustomerDto result = customerService.createCustomer(createRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        verify(customerRepository, times(1)).existsByEmail("john@example.com");
        verify(customerRepository, times(1)).save(any(CustomerEntity.class));
    }

    @Test
    @Display("Should throw exception when creating duplicate customer")
    void testCreateCustomerDuplicate() {
        // Arrange
        when(customerRepository.existsByEmail("john@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            customerService.createCustomer(createRequest);
        });
        verify(customerRepository, never()).save(any(CustomerEntity.class));
    }

    @Test
    @Display("Should retrieve all customers with pagination")
    void testGetAllCustomers() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        Page<CustomerEntity> page = new PageImpl<>(List.of(customerEntity), pageable, 1);
        when(customerRepository.findAll(pageable)).thenReturn(page);
        when(mapper.customerEntityToDto(customerEntity)).thenReturn(customerDto);

        // Act
        Page<CustomerDto> result = customerService.getAllCustomers(pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(customerRepository, times(1)).findAll(pageable);
    }

    @Test
    @Display("Should delete customer successfully")
    void testDeleteCustomer() {
        // Arrange
        when(customerRepository.existsById(1L)).thenReturn(true);

        // Act
        customerService.deleteCustomer(1L);

        // Assert
        verify(customerRepository, times(1)).deleteById(1L);
    }
}

