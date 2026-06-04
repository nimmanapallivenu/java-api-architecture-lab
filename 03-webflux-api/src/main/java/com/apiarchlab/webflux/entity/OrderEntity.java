package com.apiarchlab.webflux.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Reactive Order Entity for R2DBC
 * 
 * Represents an order in the system.
 * 
 * Note: R2DBC doesn't support @OneToMany relationships directly.
 * To fetch order items, use a separate repository query:
 * orderItemRepository.findByOrderId(orderId)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("orders")
public class OrderEntity {
    
    @Id
    private Long id;
    
    @Column("order_number")
    private String orderNumber;
    
    @Column("customer_id")
    private Long customerId;
    
    @Column("status")
    private String status;  // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    
    @Column("total_amount")
    private BigDecimal totalAmount;
    
    @Column("notes")
    private String notes;
    
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}

// Made with Bob
