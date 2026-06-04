package com.apiarchlab.webflux.repository;

import com.apiarchlab.webflux.entity.PaymentEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface PaymentRepository extends ReactiveCrudRepository<PaymentEntity, Long> {
    
    @Query("SELECT * FROM payments WHERE order_id = :orderId")
    Mono<PaymentEntity> findByOrderId(Long orderId);
}

// Made with Bob
