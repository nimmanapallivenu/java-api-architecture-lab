package com.apiarchlab.rest.dto;

import com.apiarchlab.rest.enums.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentDto {
    private Long id;

    private String transactionId;

    @NotNull(message = "Order ID is required")
    private Long orderId;

    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;

    private PaymentStatus status;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String cardLastFour;
    private String cardBrand;

    private String gatewayResponse;
    private String errorMessage;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime processedAt;
    private LocalDateTime refundedAt;

    @DecimalMin(value = "0.00")
    private BigDecimal refundAmount;
}

