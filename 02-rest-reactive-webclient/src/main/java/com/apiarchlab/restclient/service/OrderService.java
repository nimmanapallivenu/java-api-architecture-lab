package com.apiarchlab.restclient.service;

import com.apiarchlab.restclient.client.NotificationServiceClient;
import com.apiarchlab.restclient.client.PaymentServiceClient;
import com.apiarchlab.restclient.dto.CreateOrderRequest;
import com.apiarchlab.restclient.dto.OrderDto;
import com.apiarchlab.restclient.dto.PaymentDto;
import com.apiarchlab.restclient.dto.PaymentRequest;
import com.apiarchlab.restclient.entity.OrderEntity;
import com.apiarchlab.restclient.entity.OrderItemEntity;
import com.apiarchlab.restclient.entity.PaymentEntity;
import com.apiarchlab.restclient.enums.OrderStatus;
import com.apiarchlab.restclient.enums.PaymentStatus;
import com.apiarchlab.restclient.exception.BusinessException;
import com.apiarchlab.restclient.exception.ResourceNotFoundException;
import com.apiarchlab.restclient.mapper.EntityDtoMapper;
import com.apiarchlab.restclient.repository.OrderRepository;
import com.apiarchlab.restclient.repository.PaymentRepository;
import com.apiarchlab.restclient.util.OrderNumberGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

/**
 * Order Service with ASYNC WebClient calls for downstream services.
 * 
 * Key differences from Module 01:
 * 1. Uses WebClient for non-blocking calls to payment service
 * 2. Uses NotificationServiceClient for fire-and-forget notifications
 * 3. Payment processing is async (non-blocking)
 * 4. Notification sending doesn't block the request
 * 
 * This pattern:
 * - Keeps the main request fast (doesn't wait for external services)
 * - Makes better use of thread pool (threads available for other requests)
 * - Uses Netty/Reactor for non-blocking I/O
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;
    private final CustomerService customerService;
    private final PaymentServiceClient paymentServiceClient;
    private final NotificationServiceClient notificationServiceClient;
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

        // ASYNC: Send notification without blocking the request
        sendOrderCreatedNotificationAsync(saved);

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

        // ASYNC: Send cancellation notification
        sendOrderCancelledNotificationAsync(order);

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

    /**
     * Process payment ASYNCHRONOUSLY using WebClient.
     * 
     * Key: The main request returns immediately while payment processing happens in background.
     * Demonstrates how WebClient reduces thread consumption.
     */
    public PaymentDto processPayment(Long orderId, PaymentRequest request) {
        log.info("Processing payment for order id: {} (ASYNC)", orderId);

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new BusinessException("Can only process payment for pending orders", "INVALID_ORDER_STATUS");
        }

        // Check if payment already exists
        if (order.getPayment() != null) {
            throw new BusinessException("Payment already exists for this order", "PAYMENT_EXISTS");
        }

        // Create payment record (initially PENDING)
        String transactionId = "TXN-" + UUID.randomUUID().toString();
        
        PaymentEntity payment = PaymentEntity.builder()
                .transactionId(transactionId)
                .order(order)
                .amount(order.getTotalAmount())
                .status(PaymentStatus.PENDING)
                .paymentMethod(request.getPaymentMethod())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        PaymentEntity savedPayment = paymentRepository.save(payment);
        
        // ASYNC: Call payment service in background (doesn't block request)
        processPaymentAsync(order, savedPayment, request);

        log.info("Payment processing initiated for order id: {} (will complete asynchronously)", orderId);
        return mapper.paymentEntityToDto(savedPayment);
    }

    /**
     * Process payment asynchronously using WebClient.
     * This runs in background without blocking the main request.
     */
    @Async
    protected void processPaymentAsync(OrderEntity order, PaymentEntity payment, PaymentRequest request) {
        log.debug("Background task: Processing payment asynchronously for order: {}", order.getId());

        try {
            // Build payment DTO
            PaymentDto paymentDto = PaymentDto.builder()
                    .transactionId(payment.getTransactionId())
                    .orderId(order.getId())
                    .amount(order.getTotalAmount())
                    .paymentMethod(request.getPaymentMethod())
                    .build();

            // Call payment service ASYNCHRONOUSLY (non-blocking)
            paymentServiceClient.processPaymentAsync(order.getId(), paymentDto)
                    .thenAccept(result -> {
                        // Update payment with result
                        payment.setStatus(result.getStatus());
                        payment.setGatewayResponse(result.getGatewayResponse());
                        payment.setErrorMessage(result.getErrorMessage());
                        payment.setProcessedAt(LocalDateTime.now());
                        paymentRepository.save(payment);

                        // Update order status if successful
                        if (result.getStatus() == PaymentStatus.COMPLETED) {
                            order.setStatus(OrderStatus.CONFIRMED);
                            orderRepository.save(order);
                            
                            // Send success notification
                            sendPaymentSuccessNotificationAsync(order, result);
                        } else {
                            // Send failure notification
                            sendPaymentFailureNotificationAsync(order, result);
                        }

                        log.info("Payment processing completed for order: {} with status: {}", 
                                order.getId(), result.getStatus());
                    })
                    .exceptionally(ex -> {
                        log.error("Payment processing failed for order: {}", order.getId(), ex);
                        payment.setStatus(PaymentStatus.FAILED);
                        payment.setErrorMessage(ex.getMessage());
                        paymentRepository.save(payment);
                        return null;
                    });

        } catch (Exception ex) {
            log.error("Error in background payment processing", ex);
        }
    }

    //  === ASYNC NOTIFICATION METHODS === 
    
    @Async
    protected void sendOrderCreatedNotificationAsync(OrderEntity order) {
        log.debug("Sending order created notification for: {}", order.getId());
        notificationServiceClient.sendNotificationAsync(
                "customer@example.com",  // In real scenario, get from customer
                "Order Created",
                "Your order " + order.getOrderNumber() + " has been created."
        );
    }

    @Async
    protected void sendOrderCancelledNotificationAsync(OrderEntity order) {
        log.debug("Sending order cancelled notification for: {}", order.getId());
        notificationServiceClient.sendNotificationAsync(
                "customer@example.com",
                "Order Cancelled",
                "Your order " + order.getOrderNumber() + " has been cancelled."
        );
    }

    @Async
    protected void sendPaymentSuccessNotificationAsync(OrderEntity order, PaymentDto payment) {
        log.debug("Sending payment success notification for order: {}", order.getId());
        notificationServiceClient.sendPaymentConfirmation(
                payment.getTransactionId(),
                order.getTotalAmount().doubleValue(),
                "customer@example.com"
        );
    }

    @Async
    protected void sendPaymentFailureNotificationAsync(OrderEntity order, PaymentDto payment) {
        log.debug("Sending payment failure notification for order: {}", order.getId());
        notificationServiceClient.sendNotificationAsync(
                "customer@example.com",
                "Payment Failed",
                "Payment for order " + order.getOrderNumber() + " failed: " + payment.getErrorMessage()
        );
    }
}

