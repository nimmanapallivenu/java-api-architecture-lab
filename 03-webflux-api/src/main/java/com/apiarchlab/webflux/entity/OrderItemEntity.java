package com.apiarchlab.webflux.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Reactive OrderItem Entity for R2DBC
 * 
 * Represents individual items within an order.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("order_items")
public class OrderItemEntity {
    
    @Id
    private Long id;
    
    @Column("order_id")
    private Long orderId;
    
    @Column("product_name")
    private String productName;
    
    @Column("quantity")
    private Integer quantity;
    
    @Column("unit_price")
    private BigDecimal unitPrice;
    
    @Column("total_price")
    private BigDecimal totalPrice;
    
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}

// Made with Bob
