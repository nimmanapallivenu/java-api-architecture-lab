package com.apiarchlab.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.apiarchlab.domain.enums.OrderStatus;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
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
    private List<OrderItem> items = new ArrayList<>();

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime deliveredAt;

    public void addItem(OrderItem item) {
        this.items.add(item);
    }

    public BigDecimal calculateTotal() {
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return subtotal.add(taxAmount).subtract(discountAmount);
    }
}

