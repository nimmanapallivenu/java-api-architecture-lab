package com.apiarchlab.rest.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items", indexes = {
    @Index(name = "idx_order_id", columnList = "order_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @NotBlank(message = "Product ID is required")
    @Column(nullable = false, length = 100)
    private String productId;

    @NotBlank(message = "Product name is required")
    @Column(nullable = false, length = 200)
    private String productName;

    @DecimalMin(value = "0.01", message = "Unit price must be greater than 0")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private Integer quantity;

    @DecimalMin(value = "0.00", message = "Discount must be 0 or greater")
    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discount = BigDecimal.ZERO;

    public BigDecimal getTotalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity))
                .subtract(discount != null ? discount : BigDecimal.ZERO);
    }
}

