package com.apiarchlab.webflux.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateOrderRequest {
    
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotEmpty(message = "Order must have at least one item")
    private List<OrderItemRequest> items;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderItemRequest {
        @NotNull(message = "Product name is required")
        private String productName;
        
        @NotNull(message = "Quantity is required")
        private Integer quantity;
        
        @NotNull(message = "Unit price is required")
        private java.math.BigDecimal unitPrice;
    }
}

// Made with Bob
