package com.apiarchlab.webflux.repository;

import com.apiarchlab.webflux.entity.CustomerEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Reactive Repository for Customer Entity
 * 
 * Key Differences from JPA Repository:
 * - Extends ReactiveCrudRepository (not JpaRepository)
 * - All methods return Mono<T> or Flux<T>
 * - Non-blocking database operations
 * - No lazy loading or fetch strategies
 * 
 * Common Methods (inherited from ReactiveCrudRepository):
 * - Mono<CustomerEntity> findById(Long id)
 * - Flux<CustomerEntity> findAll()
 * - Mono<CustomerEntity> save(CustomerEntity entity)
 * - Mono<Void> deleteById(Long id)
 * - Mono<Long> count()
 * - Mono<Boolean> existsById(Long id)
 */
@Repository
public interface CustomerRepository extends ReactiveCrudRepository<CustomerEntity, Long> {
    
    /**
     * Find customer by email
     * Spring Data R2DBC generates query from method name
     * 
     * @param email Customer email
     * @return Mono<CustomerEntity> - 0 or 1 customer
     */
    Mono<CustomerEntity> findByEmail(String email);
    
    /**
     * Find customers by city
     * 
     * @param city City name
     * @return Flux<CustomerEntity> - 0 to N customers
     */
    Flux<CustomerEntity> findByCity(String city);
    
    /**
     * Find customers by country
     * 
     * @param country Country name
     * @return Flux<CustomerEntity> - 0 to N customers
     */
    Flux<CustomerEntity> findByCountry(String country);
    
    /**
     * Check if customer exists by email
     * 
     * @param email Customer email
     * @return Mono<Boolean> - true if exists, false otherwise
     */
    Mono<Boolean> existsByEmail(String email);
    
    /**
     * Custom query to find customers by state
     * 
     * @param state State name
     * @return Flux<CustomerEntity>
     */
    @Query("SELECT * FROM customers WHERE state = :state ORDER BY last_name, first_name")
    Flux<CustomerEntity> findByStateOrdered(String state);
    
    /**
     * Find customers created after a specific date
     * 
     * @param date Date to compare
     * @return Flux<CustomerEntity>
     */
    @Query("SELECT * FROM customers WHERE created_at > :date")
    Flux<CustomerEntity> findCreatedAfter(String date);
}

// Made with Bob
