package com.apiarchlab.rest.repository;

import com.apiarchlab.rest.entity.PaymentEntity;
import com.apiarchlab.rest.enums.PaymentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentEntity, Long> {
    Optional<PaymentEntity> findByTransactionId(String transactionId);

    Optional<PaymentEntity> findByOrderId(Long orderId);

    Page<PaymentEntity> findByStatus(PaymentStatus status, Pageable pageable);
}

