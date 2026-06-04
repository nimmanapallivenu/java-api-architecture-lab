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
 * Reactive Payment Entity for R2DBC
 * 
 * Represents payment information for orders.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("payments")
public class PaymentEntity {
    
    @Id
    private Long id;
    
    @Column("order_id")
    private Long orderId;
    
    @Column("payment_method")
    private String paymentMethod;  // CREDIT_CARD, DEBIT_CARD, PAYPAL, etc.
    
    @Column("amount")
    private BigDecimal amount;
    
    @Column("status")
    private String status;  // PENDING, COMPLETED, FAILED, REFUNDED
    
    @Column("transaction_id")
    private String transactionId;
    
    @Column("payment_date")
    private LocalDateTime paymentDate;
    
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}

// Made with Bob
