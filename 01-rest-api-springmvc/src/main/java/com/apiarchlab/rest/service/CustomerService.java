package com.apiarchlab.rest.service;

import com.apiarchlab.rest.dto.CreateCustomerRequest;
import com.apiarchlab.rest.dto.CustomerDto;
import com.apiarchlab.rest.entity.CustomerEntity;
import com.apiarchlab.rest.exception.BusinessException;
import com.apiarchlab.rest.exception.ResourceNotFoundException;
import com.apiarchlab.rest.mapper.EntityDtoMapper;
import com.apiarchlab.rest.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerService {
    private final CustomerRepository customerRepository;
    private final EntityDtoMapper mapper;

    @Transactional(readOnly = true)
    public Page<CustomerDto> getAllCustomers(Pageable pageable) {
        log.debug("Fetching customers with pagination: {}", pageable);
        return customerRepository.findAll(pageable)
                .map(mapper::customerEntityToDto);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerById(Long id) {
        log.debug("Fetching customer with id: {}", id);
        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Customer not found with id: {}", id);
                    return new ResourceNotFoundException("Customer", "id", id);
                });
        return mapper.customerEntityToDto(customer);
    }

    @Transactional(readOnly = true)
    public CustomerDto getCustomerByEmail(String email) {
        log.debug("Fetching customer with email: {}", email);
        CustomerEntity customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> {
                    log.error("Customer not found with email: {}", email);
                    return new ResourceNotFoundException("Customer", "email", email);
                });
        return mapper.customerEntityToDto(customer);
    }

    public CustomerDto createCustomer(CreateCustomerRequest request) {
        log.info("Creating new customer with email: {}", request.getEmail());

        // Validation
        if (customerRepository.existsByEmail(request.getEmail())) {
            log.error("Customer with email already exists: {}", request.getEmail());
            throw new BusinessException("Customer with email " + request.getEmail() + " already exists", "DUPLICATE_EMAIL");
        }

        CustomerEntity entity = CustomerEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .zipCode(request.getZipCode())
                .country(request.getCountry())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        CustomerEntity saved = customerRepository.save(entity);
        log.info("Customer created successfully with id: {}", saved.getId());
        return mapper.customerEntityToDto(saved);
    }

    public CustomerDto updateCustomer(Long id, CreateCustomerRequest request) {
        log.info("Updating customer with id: {}", id);

        CustomerEntity customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        // Check if email is being changed and if it's unique
        if (!customer.getEmail().equals(request.getEmail()) && 
            customerRepository.existsByEmail(request.getEmail())) {
            log.error("Customer with email already exists: {}", request.getEmail());
            throw new BusinessException("Customer with email " + request.getEmail() + " already exists", "DUPLICATE_EMAIL");
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setEmail(request.getEmail());
        customer.setPhone(request.getPhone());
        customer.setAddress(request.getAddress());
        customer.setCity(request.getCity());
        customer.setState(request.getState());
        customer.setZipCode(request.getZipCode());
        customer.setCountry(request.getCountry());
        customer.setUpdatedAt(LocalDateTime.now());

        CustomerEntity updated = customerRepository.save(customer);
        log.info("Customer updated successfully with id: {}", id);
        return mapper.customerEntityToDto(updated);
    }

    public void deleteCustomer(Long id) {
        log.info("Deleting customer with id: {}", id);

        if (!customerRepository.existsById(id)) {
            log.error("Customer not found with id: {}", id);
            throw new ResourceNotFoundException("Customer", "id", id);
        }

        customerRepository.deleteById(id);
        log.info("Customer deleted successfully with id: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long id) {
        return customerRepository.existsById(id);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }
}

