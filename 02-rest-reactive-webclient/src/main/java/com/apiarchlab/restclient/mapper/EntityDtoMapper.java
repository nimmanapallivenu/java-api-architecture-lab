package com.apiarchlab.restclient.mapper;

import com.apiarchlab.restclient.dto.CustomerDto;
import com.apiarchlab.restclient.dto.OrderDto;
import com.apiarchlab.restclient.dto.OrderItemDto;
import com.apiarchlab.restclient.dto.PaymentDto;
import com.apiarchlab.restclient.entity.CustomerEntity;
import com.apiarchlab.restclient.entity.OrderEntity;
import com.apiarchlab.restclient.entity.OrderItemEntity;
import com.apiarchlab.restclient.entity.PaymentEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityDtoMapper {

    // Customer mappings
    public CustomerDto customerEntityToDto(CustomerEntity entity) {
        if (entity == null) return null;

        return CustomerDto.builder()
                .id(entity.getId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .address(entity.getAddress())
                .city(entity.getCity())
                .state(entity.getState())
                .zipCode(entity.getZipCode())
                .country(entity.getCountry())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public CustomerEntity customerDtoToEntity(CustomerDto dto) {
        if (dto == null) return null;

        return CustomerEntity.builder()
                .id(dto.getId())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .address(dto.getAddress())
                .city(dto.getCity())
                .state(dto.getState())
                .zipCode(dto.getZipCode())
                .country(dto.getCountry())
                .build();
    }

    // Order mappings
    public OrderDto orderEntityToDto(OrderEntity entity) {
        if (entity == null) return null;

        return OrderDto.builder()
                .id(entity.getId())
                .orderNumber(entity.getOrderNumber())
                .customerId(entity.getCustomerId())
                .status(entity.getStatus())
                .totalAmount(entity.getTotalAmount())
                .discountAmount(entity.getDiscountAmount())
                .taxAmount(entity.getTaxAmount())
                .subtotalAmount(entity.getSubtotalAmount())
                .notes(entity.getNotes())
                .items(entity.getItems().stream()
                        .map(this::orderItemEntityToDto)
                        .collect(Collectors.toList()))
                .payment(entity.getPayment() != null ? paymentEntityToDto(entity.getPayment()) : null)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .deliveredAt(entity.getDeliveredAt())
                .build();
    }

    public OrderEntity orderDtoToEntity(OrderDto dto) {
        if (dto == null) return null;

        return OrderEntity.builder()
                .id(dto.getId())
                .orderNumber(dto.getOrderNumber())
                .customerId(dto.getCustomerId())
                .status(dto.getStatus())
                .totalAmount(dto.getTotalAmount())
                .discountAmount(dto.getDiscountAmount())
                .taxAmount(dto.getTaxAmount())
                .subtotalAmount(dto.getSubtotalAmount())
                .notes(dto.getNotes())
                .build();
    }

    // OrderItem mappings
    public OrderItemDto orderItemEntityToDto(OrderItemEntity entity) {
        if (entity == null) return null;

        return OrderItemDto.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .productName(entity.getProductName())
                .unitPrice(entity.getUnitPrice())
                .quantity(entity.getQuantity())
                .discount(entity.getDiscount())
                .build();
    }

    public OrderItemEntity orderItemDtoToEntity(OrderItemDto dto) {
        if (dto == null) return null;

        return OrderItemEntity.builder()
                .id(dto.getId())
                .productId(dto.getProductId())
                .productName(dto.getProductName())
                .unitPrice(dto.getUnitPrice())
                .quantity(dto.getQuantity())
                .discount(dto.getDiscount())
                .build();
    }

    // Payment mappings
    public PaymentDto paymentEntityToDto(PaymentEntity entity) {
        if (entity == null) return null;

        return PaymentDto.builder()
                .id(entity.getId())
                .transactionId(entity.getTransactionId())
                .orderId(entity.getOrder() != null ? entity.getOrder().getId() : null)
                .amount(entity.getAmount())
                .status(entity.getStatus())
                .paymentMethod(entity.getPaymentMethod())
                .cardLastFour(entity.getCardLastFour())
                .cardBrand(entity.getCardBrand())
                .gatewayResponse(entity.getGatewayResponse())
                .errorMessage(entity.getErrorMessage())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .processedAt(entity.getProcessedAt())
                .refundedAt(entity.getRefundedAt())
                .refundAmount(entity.getRefundAmount())
                .build();
    }

    public PaymentEntity paymentDtoToEntity(PaymentDto dto) {
        if (dto == null) return null;

        return PaymentEntity.builder()
                .id(dto.getId())
                .transactionId(dto.getTransactionId())
                .amount(dto.getAmount())
                .status(dto.getStatus())
                .paymentMethod(dto.getPaymentMethod())
                .cardLastFour(dto.getCardLastFour())
                .cardBrand(dto.getCardBrand())
                .build();
    }

    // List mappings
    public List<CustomerDto> customerEntitiesToDtos(List<CustomerEntity> entities) {
        return entities.stream()
                .map(this::customerEntityToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> orderEntitiesToDtos(List<OrderEntity> entities) {
        return entities.stream()
                .map(this::orderEntityToDto)
                .collect(Collectors.toList());
    }
}

