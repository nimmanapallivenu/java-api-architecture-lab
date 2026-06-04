package com.apiarchlab.rest.service;

import com.apiarchlab.rest.dto.CreateOrderRequest;
import com.apiarchlab.rest.dto.OrderDto;
import com.apiarchlab.rest.dto.PaymentRequest;
import com.apiarchlab.rest.dto.PaymentDto;
import com.apiarchlab.rest.entity.OrderEntity;
import com.apiarchlab.rest.entity.OrderItemEntity;
import com.apiarchlab.rest.entity.PaymentEntity;
import com.apiarchlab.rest.enums.OrderStatus;
import com.apiarchlab.rest.enums.PaymentStatus;
import com.apiarchlab.rest.exception.BusinessException;
import com.apiarchlab.rest.exception.ResourceNotFoundException;
import com.apiarchlab.rest.mapper.EntityDtoMapper;
import com.apiarchlab.rest.repository.OrderRepository;
import com.apiarchlab.rest.repository.PaymentRepository;
import com.apiarchlab.rest.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;
    private final EntityDtoMapper mapper;
    private final OrderNumberGenerator orderNumberGenerator;

    @Transactional(readOnly = true)
    public Page<OrderDto> getAllOrders(Pageable pageable) {
        log.debug("Fetching all orders with pagination: {}", pageable);
        return orderRepository.findAll(pageable)
                .map(mapper::orderEntityToDto);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderById(Long id) {
        log.debug("Fetching order with id: {}", id);
        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
        return mapper.orderEntityToDto(order);
    }

    @Transactional(readOnly = true)
    public OrderDto getOrderByNumber(String orderNumber) {
        log.debug("Fetching order with number: {}", orderNumber);
        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "orderNumber", orderNumber));
        return mapper.orderEntityToDto(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByCustomerId(Long customerId, Pageable pageable) {
        log.debug("Fetching orders for customer id: {} with pagination: {}", customerId, pageable);
        
        // Verify customer exists
        if (!customerService.existsById(customerId)) {
            throw new ResourceNotFoundException("Customer", "id", customerId);
        }

        return orderRepository.findByCustomerId(customerId, pageable)
                .map(mapper::orderEntityToDto);
    }

    @Transactional(readOnly = true)
    public Page<OrderDto> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        log.debug("Fetching orders with status: {} and pagination: {}", status, pageable);
        return orderRepository.findByStatus(status, pageable)
                .map(mapper::orderEntityToDto);
    }

    public OrderDto createOrder(CreateOrderRequest request) {
        log.info("Creating new order for customer id: {}", request.getCustomerId());

        // Validate customer exists
        if (!customerService.existsById(request.getCustomerId())) {
            throw new ResourceNotFoundException("Customer", "id", request.getCustomerId());
        }

        // Validate order has items
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Order must have at least one item", "EMPTY_ORDER");
        }

        // Create order
        String orderNumber = orderNumberGenerator.generateOrderNumber();
        
        OrderEntity order = OrderEntity.builder()
                .orderNumber(orderNumber)
                .customerId(request.getCustomerId())
                .status(OrderStatus.PENDING)
                .discountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO)
                .taxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO)
                .notes(request.getNotes())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // Add items
        for (var itemDto : request.getItems()) {
            OrderItemEntity item = OrderItemEntity.builder()
                    .productId(itemDto.getProductId())
                    .productName(itemDto.getProductName())
                    .unitPrice(itemDto.getUnitPrice())
                    .quantity(itemDto.getQuantity())
                    .discount(itemDto.getDiscount() != null ? itemDto.getDiscount() : BigDecimal.ZERO)
                    .build();
            order.addItem(item);
        }

        // Calculate total
        BigDecimal subtotal = order.getItems().stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .subtract(item.getDiscount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotalAmount(subtotal);
        order.setTotalAmount(order.calculateTotal());

        OrderEntity saved = orderRepository.save(order);
        log.info("Order created successfully with number: {}", orderNumber);
        return mapper.orderEntityToDto(saved);
    }

    public OrderDto updateOrder(Long id, CreateOrderRequest request) {
        log.info("Updating order with id: {}", id);

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        // Only allow updating pending orders
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Can only update pending orders", "INVALID_ORDER_STATUS");
        }

        order.setNotes(request.getNotes());
        order.setDiscountAmount(request.getDiscountAmount() != null ? request.getDiscountAmount() : BigDecimal.ZERO);
        order.setTaxAmount(request.getTaxAmount() != null ? request.getTaxAmount() : BigDecimal.ZERO);
        order.setUpdatedAt(LocalDateTime.now());

        // Update items
        order.getItems().clear();
        for (var itemDto : request.getItems()) {
            OrderItemEntity item = OrderItemEntity.builder()
                    .productId(itemDto.getProductId())
                    .productName(itemDto.getProductName())
                    .unitPrice(itemDto.getUnitPrice())
                    .quantity(itemDto.getQuantity())
                    .discount(itemDto.getDiscount() != null ? itemDto.getDiscount() : BigDecimal.ZERO)
                    .build();
            order.addItem(item);
        }

        // Recalculate total
        BigDecimal subtotal = order.getItems().stream()
                .map(item -> item.getUnitPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()))
                        .subtract(item.getDiscount()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotalAmount(subtotal);
        order.setTotalAmount(order.calculateTotal());

        OrderEntity updated = orderRepository.save(order);
        log.info("Order updated successfully with id: {}", id);
        return mapper.orderEntityToDto(updated);
    }

    public void cancelOrder(Long id) {
        log.info("Cancelling order with id: {}", id);

        OrderEntity order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            throw new BusinessException("Cannot cancel order in status: " + order.getStatus(), "INVALID_ORDER_STATUS");
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Order cancelled successfully with id: {}", id);
    }

    public void deleteOrder(Long id) {
        log.info("Deleting order with id: {}", id);

        if (!orderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Order", "id", id);
        }

        orderRepository.deleteById(id);
        log.info("Order deleted successfully with id: {}", id);
    }

    public PaymentDto processPayment(Long orderId, PaymentRequest request) {
        log.info("Processing payment for order id: {}", orderId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Can only process payment for pending orders", "INVALID_ORDER_STATUS");
        }

        // Check if payment already exists
        if (order.getPayment() != null) {
            throw new BusinessException("Payment already exists for this order", "PAYMENT_EXISTS");
        }

        // Create payment record
        String transactionId = "TXN-" + UUID.randomUUID().toString();
        
        PaymentEntity payment = PaymentEntity.builder()
                .transactionId(transactionId)
                .order(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.COMPLETED) // Simplified - normally would call payment gateway
                .paymentMethod(request.getPaymentMethod())
                .cardLastFour("1234") // Simplified
                .cardBrand(extractCardBrand(request.getPaymentMethod()))
                .processedAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PaymentEntity savedPayment = paymentRepository.save(payment);
        
        // Update order status
        order.setStatus(OrderStatus.CONFIRMED);
        order.setUpdatedAt(LocalDateTime.now());
        orderRepository.save(order);

        log.info("Payment processed successfully for order id: {} with transaction id: {}", orderId, transactionId);
        return mapper.paymentEntityToDto(savedPayment);
    }

    private String extractCardBrand(String paymentMethod) {
        if (paymentMethod.toLowerCase().contains("visa")) return "VISA";
        if (paymentMethod.toLowerCase().contains("mastercard")) return "MASTERCARD";
        if (paymentMethod.toLowerCase().contains("amex")) return "AMEX";
        return "OTHER";
    }
}

