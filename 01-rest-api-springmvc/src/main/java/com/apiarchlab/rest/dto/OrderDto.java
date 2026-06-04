package com.apiarchlab.rest.dto;

import com.apiarchlab.rest.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDto {
    private Long id;

    private String orderNumber;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private OrderStatus status;

    @DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
    private BigDecimal totalAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal discountAmount;

    @DecimalMin(value = "0.00")
    private BigDecimal taxAmount;

    @DecimalMin(value = "0.01")
    private BigDecimal subtotalAmount;

    private String notes;

    @Valid
    @Builder.Default
    private List<OrderItemDto> items = new ArrayList<>();

    private PaymentDto payment;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;

    public BigDecimal calculateTotal() {
        BigDecimal subtotal = items.stream()
                .map(item -> item.getTotalPrice())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return subtotal.add(taxAmount != null ? taxAmount : BigDecimal.ZERO)
                .subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    }
}

