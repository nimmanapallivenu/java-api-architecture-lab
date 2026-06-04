package com.apiarchlab.webflux.repository;

import com.apiarchlab.webflux.entity.OrderEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, Long> {
    Mono<OrderEntity> findByOrderNumber(String orderNumber);
    Flux<OrderEntity> findByCustomerId(Long customerId);
    Flux<OrderEntity> findByStatus(String status);
    
    @Query("SELECT * FROM orders WHERE customer_id = :customerId AND status = :status")
    Flux<OrderEntity> findByCustomerIdAndStatus(Long customerId, String status);
}

// Made with Bob
