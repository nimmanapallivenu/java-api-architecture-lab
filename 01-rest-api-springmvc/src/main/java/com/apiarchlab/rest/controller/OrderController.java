package com.apiarchlab.rest.controller;

import com.apiarchlab.rest.dto.CreateOrderRequest;
import com.apiarchlab.rest.dto.OrderDto;
import com.apiarchlab.rest.dto.PaymentDto;
import com.apiarchlab.rest.dto.PaymentRequest;
import com.apiarchlab.rest.enums.OrderStatus;
import com.apiarchlab.rest.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Orders", description = "APIs for managing orders")
public class OrderController {
    private final OrderService orderService;

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve paginated list of all orders")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved orders")
    public ResponseEntity<Page<OrderDto>> getAllOrders(
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        log.info("GET /api/orders - Fetching all orders");
        return ResponseEntity.ok(orderService.getAllOrders(pageable));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve a specific order by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderDto> getOrderById(
            @Parameter(description = "Order ID")
            @PathVariable Long id) {
        log.info("GET /api/orders/{} - Fetching order", id);
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    @GetMapping("/number/{orderNumber}")
    @Operation(summary = "Get order by number", description = "Retrieve order by order number")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order found"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderDto> getOrderByNumber(
            @Parameter(description = "Order number")
            @PathVariable String orderNumber) {
        log.info("GET /api/orders/number/{} - Fetching order by number", orderNumber);
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get orders by customer", description = "Retrieve all orders for a specific customer")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Orders retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<Page<OrderDto>> getOrdersByCustomerId(
            @Parameter(description = "Customer ID")
            @PathVariable Long customerId,
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        log.info("GET /api/orders/customer/{} - Fetching orders for customer", customerId);
        return ResponseEntity.ok(orderService.getOrdersByCustomerId(customerId, pageable));
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Retrieve orders filtered by status")
    @ApiResponse(responseCode = "200", description = "Orders retrieved successfully")
    public ResponseEntity<Page<OrderDto>> getOrdersByStatus(
            @Parameter(description = "Order status")
            @PathVariable OrderStatus status,
            @Parameter(description = "Pagination parameters")
            Pageable pageable) {
        log.info("GET /api/orders/status/{} - Fetching orders with status", status);
        return ResponseEntity.ok(orderService.getOrdersByStatus(status, pageable));
    }

    @PostMapping
    @Operation(summary = "Create new order", description = "Create a new order with items")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Order created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request data"),
        @ApiResponse(responseCode = "404", description = "Customer not found")
    })
    public ResponseEntity<OrderDto> createOrder(
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("POST /api/orders - Creating new order for customer: {}", request.getCustomerId());
        OrderDto created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update order", description = "Update an existing order (only pending orders)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Order updated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request or order status"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<OrderDto> updateOrder(
            @Parameter(description = "Order ID")
            @PathVariable Long id,
            @Valid @RequestBody CreateOrderRequest request) {
        log.info("PUT /api/orders/{} - Updating order", id);
        OrderDto updated = orderService.updateOrder(id, request);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel order", description = "Cancel a pending or confirmed order")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Order cancelled successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot cancel order in current status"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Void> cancelOrder(
            @Parameter(description = "Order ID")
            @PathVariable Long id) {
        log.info("POST /api/orders/{}/cancel - Cancelling order", id);
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete order", description = "Delete an order by ID")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Order deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<Void> deleteOrder(
            @Parameter(description = "Order ID")
            @PathVariable Long id) {
        log.info("DELETE /api/orders/{} - Deleting order", id);
        orderService.deleteOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/payment")
    @Operation(summary = "Process payment", description = "Process payment for an order")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Payment processed successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid payment or order status"),
        @ApiResponse(responseCode = "404", description = "Order not found")
    })
    public ResponseEntity<PaymentDto> processPayment(
            @Parameter(description = "Order ID")
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request) {
        log.info("POST /api/orders/{}/payment - Processing payment", id);
        PaymentDto payment = orderService.processPayment(id, request);
        return ResponseEntity.ok(payment);
    }
}

