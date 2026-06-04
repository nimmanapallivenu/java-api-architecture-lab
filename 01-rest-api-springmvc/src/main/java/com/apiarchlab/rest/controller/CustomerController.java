package com.apiarchlab.rest.controller;

import com.apiarchlab.rest.dto.CreateCustomerRequest;
import com.apiarchlab.rest.dto.CustomerDto;
import com.apiarchlab.rest.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customers")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Customers", description = "APIs for managing customers")
public class CustomerController {
    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieve paginated list of all customers")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved customers")
    public ResponseEntity<Page<CustomerDto>> getAllCustomers(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        log.info("GET /api/customers - Fetching all customers");
        return ResponseEntity.ok(customerService.getAllCustomers(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieve a specific customer by their ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer found"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<CustomerDto> getCustomerById(
            @Parameter(description = "Customer ID")
            @PathVariable Long id) {
        log.info("GET /api/customers/{} - Fetching customer", id);
        return ResponseEntity.ok(customerService.getCustomerById(id));
    }

    @GetMapping("/by-email/{email}")
    @Operation(summary = "Get customer by email", description = "Retrieve customer by email address")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer found"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<CustomerDto> getCustomerByEmail(
            @Parameter(description = "Customer email")
            @PathVariable String email) {
        log.info("GET /api/customers/by-email/{} - Fetching customer by email", email);
        return ResponseEntity.ok(customerService.getCustomerByEmail(email));
    }

    @PostMapping
    @Operation(summary = "Create new customer", description = "Create a new customer account")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Customer created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "409", description = "Customer already exists")
    })
    public ResponseEntity<CustomerDto> createCustomer(
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("POST /api/customers - Creating new customer with email: {}", request.getEmail());
        CustomerDto created = customerService.createCustomer(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Update an existing customer's information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Customer updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<CustomerDto> updateCustomer(
            @Parameter(description = "Customer ID")
            @PathVariable Long id,
            @Valid @RequestBody CreateCustomerRequest request) {
        log.info("PUT /api/customers/{} - Updating customer", id);
        CustomerDto updated = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Delete a customer by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Customer deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID")
            @PathVariable Long id) {
        log.info("DELETE /api/customers/{} - Deleting customer", id);
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}

