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

// Made with Bob
