package com.apiarchlab.restclient.repository;

import com.apiarchlab.restclient.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {
    Optional<CustomerEntity> findByEmail(String email);

    Optional<CustomerEntity> findByPhone(String phone);

    @Query("SELECT c FROM CustomerEntity c WHERE c.email = :email OR c.phone = :phone")
    Optional<CustomerEntity> findByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);

    boolean existsByEmail(String email);
}

