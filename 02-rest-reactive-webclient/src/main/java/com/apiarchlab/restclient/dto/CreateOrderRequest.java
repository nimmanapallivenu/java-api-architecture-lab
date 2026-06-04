package com.apiarchlab.restclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @Valid
    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemDto> items;

    @DecimalMin(value = "0.00")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @DecimalMin(value = "0.00")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    private String notes;
}

