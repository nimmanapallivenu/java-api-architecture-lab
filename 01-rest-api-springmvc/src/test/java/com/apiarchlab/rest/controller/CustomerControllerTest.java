package com.apiarchlab.rest.controller;

import com.apiarchlab.rest.dto.CreateCustomerRequest;
import com.apiarchlab.rest.dto.CustomerDto;
import com.apiarchlab.rest.service.CustomerService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Display;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Display("Customer Controller Integration Tests")
class CustomerControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CustomerService customerService;

    private CustomerDto customerDto;
    private CreateCustomerRequest createRequest;

    @BeforeEach
    void setUp() {
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
    @Display("GET /customers should return all customers with pagination")
    void testGetAllCustomers() throws Exception {
        // Arrange
        var page = new PageImpl<>(List.of(customerDto), PageRequest.of(0, 10), 1);
        when(customerService.getAllCustomers(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/customers")
                        .param("page", "0")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @Display("GET /customers/{id} should return customer when found")
    void testGetCustomerById() throws Exception {
        // Arrange
        when(customerService.getCustomerById(1L)).thenReturn(customerDto);

        // Act & Assert
        mockMvc.perform(get("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @Display("POST /customers should create new customer")
    void testCreateCustomer() throws Exception {
        // Arrange
        when(customerService.createCustomer(any(CreateCustomerRequest.class)))
                .thenReturn(customerDto);

        // Act & Assert
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @Display("POST /customers should return 400 for invalid email")
    void testCreateCustomerInvalidEmail() throws Exception {
        // Arrange
        var invalidRequest = CreateCustomerRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("invalid-email")
                .phone("9876543210")
                .address("123 Main St")
                .build();

        // Act & Assert
        mockMvc.perform(post("/api/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    @Display("PUT /customers/{id} should update existing customer")
    void testUpdateCustomer() throws Exception {
        // Arrange
        when(customerService.updateCustomer(eq(1L), any(CreateCustomerRequest.class)))
                .thenReturn(customerDto);

        // Act & Assert
        mockMvc.perform(put("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @Display("DELETE /customers/{id} should delete customer")
    void testDeleteCustomer() throws Exception {
        // Arrange
        doNothing().when(customerService).deleteCustomer(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/customers/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(customerService, times(1)).deleteCustomer(1L);
    }
}

