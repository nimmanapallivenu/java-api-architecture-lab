package com.apiarchlab.webflux.controller;

import com.apiarchlab.webflux.dto.CreateCustomerRequest;
import com.apiarchlab.webflux.dto.CustomerDto;
import com.apiarchlab.webflux.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerDto> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @GetMapping("/{id}")
    public Mono<CustomerDto> getCustomer(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @GetMapping
    public Flux<CustomerDto> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/search")
    public Flux<CustomerDto> searchCustomers(@RequestParam String name) {
        return customerService.searchCustomers(name);
    }

    @PutMapping("/{id}")
    public Mono<CustomerDto> updateCustomer(@PathVariable Long id, @Valid @RequestBody CreateCustomerRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCustomer(@PathVariable Long id) {
        return customerService.deleteCustomer(id);
    }
}

// Made with Bob
