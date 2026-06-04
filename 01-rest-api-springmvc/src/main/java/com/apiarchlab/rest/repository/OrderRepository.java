package com.apiarchlab.rest.repository;

import com.apiarchlab.rest.entity.OrderEntity;
import com.apiarchlab.rest.enums.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, Long> {
    Optional<OrderEntity> findByOrderNumber(String orderNumber);

    Page<OrderEntity> findByCustomerId(Long customerId, Pageable pageable);

    Page<OrderEntity> findByStatus(OrderStatus status, Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.customerId = :customerId AND o.status = :status")
    Page<OrderEntity> findByCustomerIdAndStatus(
            @Param("customerId") Long customerId,
            @Param("status") OrderStatus status,
            Pageable pageable);

    @Query("SELECT o FROM OrderEntity o WHERE o.createdAt BETWEEN :startDate AND :endDate ORDER BY o.createdAt DESC")
    List<OrderEntity> findOrdersByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query(value = "SELECT o FROM OrderEntity o WHERE o.totalAmount > :minAmount AND o.totalAmount < :maxAmount",
           countQuery = "SELECT COUNT(o) FROM OrderEntity o WHERE o.totalAmount > ?1 AND o.totalAmount < ?2")
    Page<OrderEntity> findOrdersByAmountRange(
            @Param("minAmount") java.math.BigDecimal minAmount,
            @Param("maxAmount") java.math.BigDecimal maxAmount,
            Pageable pageable);
}

