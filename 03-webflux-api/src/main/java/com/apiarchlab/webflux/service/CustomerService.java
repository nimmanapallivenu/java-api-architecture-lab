package com.apiarchlab.webflux.service;

import com.apiarchlab.webflux.dto.CreateCustomerRequest;
import com.apiarchlab.webflux.dto.CustomerDto;
import com.apiarchlab.webflux.entity.CustomerEntity;
import com.apiarchlab.webflux.exception.ResourceNotFoundException;
import com.apiarchlab.webflux.mapper.EntityDtoMapper;
import com.apiarchlab.webflux.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final EntityDtoMapper mapper;

    public Mono<CustomerDto> createCustomer(CreateCustomerRequest request) {
        log.info("Creating customer: {}", request.getEmail());
        
        return customerRepository.findByEmail(request.getEmail())
                .flatMap(existing -> Mono.<CustomerDto>error(
                        new IllegalArgumentException("Customer with email " + request.getEmail() + " already exists")))
                .switchIfEmpty(Mono.defer(() -> {
                    CustomerEntity entity = mapper.dtoToEntity(request);
                    return customerRepository.save(entity)
                            .map(mapper::entityToDto)
                            .doOnSuccess(dto -> log.info("Customer created with ID: {}", dto.getId()));
                }));
    }

    public Mono<CustomerDto> getCustomerById(Long id) {
        log.info("Fetching customer by ID: {}", id);
        
        return customerRepository.findById(id)
                .map(mapper::entityToDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer not found with ID: " + id)));
    }

    public Mono<CustomerDto> getCustomerByEmail(String email) {
        log.info("Fetching customer by email: {}", email);
        
        return customerRepository.findByEmail(email)
                .map(mapper::entityToDto)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer not found with email: " + email)));
    }

    public Flux<CustomerDto> getAllCustomers() {
        log.info("Fetching all customers");
        
        return customerRepository.findAll()
                .map(mapper::entityToDto);
    }

    public Mono<CustomerDto> updateCustomer(Long id, CreateCustomerRequest request) {
        log.info("Updating customer ID: {}", id);
        
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer not found with ID: " + id)))
                .flatMap(existing -> {
                    existing.setName(request.getName());
                    existing.setEmail(request.getEmail());
                    existing.setPhone(request.getPhone());
                    existing.setAddress(request.getAddress());
                    return customerRepository.save(existing);
                })
                .map(mapper::entityToDto)
                .doOnSuccess(dto -> log.info("Customer updated: {}", dto.getId()));
    }

    public Mono<Void> deleteCustomer(Long id) {
        log.info("Deleting customer ID: {}", id);
        
        return customerRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer not found with ID: " + id)))
                .flatMap(customer -> customerRepository.deleteById(id))
                .doOnSuccess(v -> log.info("Customer deleted: {}", id));
    }

    public Flux<CustomerDto> searchCustomers(String name) {
        log.info("Searching customers by name: {}", name);
        
        return customerRepository.findByNameContainingIgnoreCase(name)
                .map(mapper::entityToDto);
    }
}

// Made with Bob
