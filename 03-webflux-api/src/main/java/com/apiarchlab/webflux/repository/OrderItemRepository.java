package com.apiarchlab.webflux.repository;

import com.apiarchlab.webflux.entity.OrderItemEntity;
import org.springframework.data.r2dbc.repository.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemEntity, Long> {
    Flux<OrderItemEntity> findByOrderId(Long orderId);
}

// Made with Bob
