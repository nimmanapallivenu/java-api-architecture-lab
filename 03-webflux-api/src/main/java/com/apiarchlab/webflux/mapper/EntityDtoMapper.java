package com.apiarchlab.webflux.mapper;

import com.apiarchlab.webflux.dto.*;
import com.apiarchlab.webflux.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityDtoMapper {

    // Customer mappings
    public CustomerDto entityToDto(CustomerEntity entity) {
        return CustomerDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CustomerEntity dtoToEntity(CreateCustomerRequest request) {
        return CustomerEntity.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .address(request.getAddress())
                .build();
    }

    // Order mappings
    public OrderDto entityToDto(OrderEntity entity, List<OrderItemEntity> items, PaymentEntity payment) {
        return OrderDto.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .customerId(entity.getCustomerId())
                .customerName(entity.getCustomerName())
                .totalAmount(entity.getTotalAmount())
                .status(entity.getStatus())
                .items(items.stream().map(this::itemEntityToDto).collect(Collectors.toList()))
                .payment(payment != null ? paymentEntityToDto(payment) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public OrderEntity orderRequestToEntity(CreateOrderRequest request, String orderNumber, String customerName) {
        return OrderEntity.builder()
                .orderNumber(orderNumber)
                .customerId(request.getCustomerId())
                .customerName(customerName)
                .status("PENDING")
                .build();
    }

    // OrderItem mappings
    public OrderItemDto itemEntityToDto(OrderItemEntity entity) {
        return OrderItemDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .productName(entity.getProductName())
                .quantity(entity.getQuantity())
                .unitPrice(entity.getUnitPrice())
                .totalPrice(entity.getTotalPrice())
                .build();
    }

    public OrderItemEntity itemRequestToEntity(CreateOrderRequest.OrderItemRequest request, Long orderId) {
        return OrderItemEntity.builder()
                .orderId(orderId)
                .productName(request.getProductName())
                .quantity(request.getQuantity())
                .unitPrice(request.getUnitPrice())
                .totalPrice(request.getUnitPrice().multiply(java.math.BigDecimal.valueOf(request.getQuantity())))
                .build();
    }

    // Payment mappings
    public PaymentDto paymentEntityToDto(PaymentEntity entity) {
        return PaymentDto.builder()
                .id(entity.getId())
                .orderId(entity.getOrderId())
                .amount(entity.getAmount())
                .paymentMethod(entity.getPaymentMethod())
                .status(entity.getStatus())
                .transactionId(entity.getTransactionId())
                .paidAt(entity.getPaidAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

// Made with Bob
