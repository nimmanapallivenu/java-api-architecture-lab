#!/bin/bash

# This script creates all remaining WebFlux API files

echo "Creating remaining WebFlux API files..."

# Create OrderService
cat > src/main/java/com/apiarchlab/webflux/service/OrderService.java << 'ORDERSERVICE'
package com.apiarchlab.webflux.service;

import com.apiarchlab.webflux.dto.CreateOrderRequest;
import com.apiarchlab.webflux.dto.OrderDto;
import com.apiarchlab.webflux.entity.OrderEntity;
import com.apiarchlab.webflux.entity.OrderItemEntity;
import com.apiarchlab.webflux.exception.ResourceNotFoundException;
import com.apiarchlab.webflux.mapper.EntityDtoMapper;
import com.apiarchlab.webflux.repository.CustomerRepository;
import com.apiarchlab.webflux.repository.OrderItemRepository;
import com.apiarchlab.webflux.repository.OrderRepository;
import com.apiarchlab.webflux.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CustomerRepository customerRepository;
    private final PaymentRepository paymentRepository;
    private final EntityDtoMapper mapper;

    public Mono<OrderDto> createOrder(CreateOrderRequest request) {
        log.info("Creating order for customer ID: {}", request.getCustomerId());
        
        return customerRepository.findById(request.getCustomerId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer not found")))
                .flatMap(customer -> {
                    String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                    OrderEntity orderEntity = mapper.orderRequestToEntity(request, orderNumber, customer.getName());
                    
                    BigDecimal totalAmount = request.getItems().stream()
                            .map(item -> item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                    orderEntity.setTotalAmount(totalAmount);
                    
                    return orderRepository.save(orderEntity)
                            .flatMap(savedOrder -> {
                                List<OrderItemEntity> items = request.getItems().stream()
                                        .map(item -> mapper.itemRequestToEntity(item, savedOrder.getId()))
                                        .toList();
                                
                                return orderItemRepository.saveAll(items)
                                        .collectList()
                                        .flatMap(savedItems -> 
                                                paymentRepository.findByOrderId(savedOrder.getId())
                                                        .map(payment -> mapper.entityToDto(savedOrder, savedItems, payment))
                                                        .defaultIfEmpty(mapper.entityToDto(savedOrder, savedItems, null))
                                        );
                            });
                })
                .doOnSuccess(dto -> log.info("Order created: {}", dto.getOrderNumber()));
    }

    public Mono<OrderDto> getOrderById(Long id) {
        log.info("Fetching order by ID: {}", id);
        
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found with ID: " + id)))
                .flatMap(order -> 
                        orderItemRepository.findByOrderId(order.getId())
                                .collectList()
                                .flatMap(items ->
                                        paymentRepository.findByOrderId(order.getId())
                                                .map(payment -> mapper.entityToDto(order, items, payment))
                                                .defaultIfEmpty(mapper.entityToDto(order, items, null))
                                )
                );
    }

    public Flux<OrderDto> getAllOrders() {
        log.info("Fetching all orders");
        
        return orderRepository.findAll()
                .flatMap(order ->
                        orderItemRepository.findByOrderId(order.getId())
                                .collectList()
                                .flatMap(items ->
                                        paymentRepository.findByOrderId(order.getId())
                                                .map(payment -> mapper.entityToDto(order, items, payment))
                                                .defaultIfEmpty(mapper.entityToDto(order, items, null))
                                )
                );
    }

    public Flux<OrderDto> getOrdersByCustomerId(Long customerId) {
        log.info("Fetching orders for customer ID: {}", customerId);
        
        return orderRepository.findByCustomerId(customerId)
                .flatMap(order ->
                        orderItemRepository.findByOrderId(order.getId())
                                .collectList()
                                .flatMap(items ->
                                        paymentRepository.findByOrderId(order.getId())
                                                .map(payment -> mapper.entityToDto(order, items, payment))
                                                .defaultIfEmpty(mapper.entityToDto(order, items, null))
                                )
                );
    }

    public Mono<OrderDto> updateOrderStatus(Long id, String status) {
        log.info("Updating order {} status to: {}", id, status);
        
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found with ID: " + id)))
                .flatMap(order -> {
                    order.setStatus(status);
                    return orderRepository.save(order);
                })
                .flatMap(order ->
                        orderItemRepository.findByOrderId(order.getId())
                                .collectList()
                                .flatMap(items ->
                                        paymentRepository.findByOrderId(order.getId())
                                                .map(payment -> mapper.entityToDto(order, items, payment))
                                                .defaultIfEmpty(mapper.entityToDto(order, items, null))
                                )
                )
                .doOnSuccess(dto -> log.info("Order status updated: {}", dto.getOrderNumber()));
    }

    public Mono<Void> deleteOrder(Long id) {
        log.info("Deleting order ID: {}", id);
        
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Order not found with ID: " + id)))
                .flatMap(order ->
                        orderItemRepository.findByOrderId(id)
                                .flatMap(item -> orderItemRepository.deleteById(item.getId()))
                                .then(orderRepository.deleteById(id))
                )
                .doOnSuccess(v -> log.info("Order deleted: {}", id));
    }
}
ORDERSERVICE

echo "OrderService created"

# Create CustomerController
cat > src/main/java/com/apiarchlab/webflux/controller/CustomerController.java << 'CUSTOMERCONTROLLER'
package com.apiarchlab.webflux.controller;

import com.apiarchlab.webflux.dto.CreateCustomerRequest;
import com.apiarchlab.webflux.dto.CustomerDto;
import com.apiarchlab.webflux.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<CustomerDto> createCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        return customerService.createCustomer(request);
    }

    @GetMapping("/{id}")
    public Mono<CustomerDto> getCustomer(@PathVariable Long id) {
        return customerService.getCustomerById(id);
    }

    @GetMapping
    public Flux<CustomerDto> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @GetMapping("/search")
    public Flux<CustomerDto> searchCustomers(@RequestParam String name) {
        return customerService.searchCustomers(name);
    }

    @PutMapping("/{id}")
    public Mono<CustomerDto> updateCustomer(@PathVariable Long id, @Valid @RequestBody CreateCustomerRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteCustomer(@PathVariable Long id) {
        return customerService.deleteCustomer(id);
    }
}
CUSTOMERCONTROLLER

echo "CustomerController created"

# Create OrderController
cat > src/main/java/com/apiarchlab/webflux/controller/OrderController.java << 'ORDERCONTROLLER'
package com.apiarchlab.webflux.controller;

import com.apiarchlab.webflux.dto.CreateOrderRequest;
import com.apiarchlab.webflux.dto.OrderDto;
import com.apiarchlab.webflux.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<OrderDto> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request);
    }

    @GetMapping("/{id}")
    public Mono<OrderDto> getOrder(@PathVariable Long id) {
        return orderService.getOrderById(id);
    }

    @GetMapping
    public Flux<OrderDto> getAllOrders() {
        return orderService.getAllOrders();
    }

    @GetMapping("/customer/{customerId}")
    public Flux<OrderDto> getOrdersByCustomer(@PathVariable Long customerId) {
        return orderService.getOrdersByCustomerId(customerId);
    }

    @PatchMapping("/{id}/status")
    public Mono<OrderDto> updateOrderStatus(@PathVariable Long id, @RequestParam String status) {
        return orderService.updateOrderStatus(id, status);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteOrder(@PathVariable Long id) {
        return orderService.deleteOrder(id);
    }
}
ORDERCONTROLLER

echo "OrderController created"

echo "All remaining files created successfully!"
