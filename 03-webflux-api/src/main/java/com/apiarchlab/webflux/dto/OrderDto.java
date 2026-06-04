package com.apiarchlab.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long id;
    private String orderNumber;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private String status;
    private List<OrderItemDto> items;
    private PaymentDto payment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

// Made with Bob
