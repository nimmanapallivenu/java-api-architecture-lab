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
