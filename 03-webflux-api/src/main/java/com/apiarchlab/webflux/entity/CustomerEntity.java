package com.apiarchlab.webflux.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

/**
 * Reactive Customer Entity for R2DBC
 * 
 * Key Differences from JPA Entity:
 * - Uses @Table from spring-data-relational (not jakarta.persistence)
 * - Uses @Id from spring-data-annotation (not jakarta.persistence)
 * - No @Entity annotation
 * - No @GeneratedValue (R2DBC handles auto-increment differently)
 * - No lazy loading or fetch strategies
 * - No @OneToMany relationships (R2DBC doesn't support them directly)
 * - Simpler, more lightweight
 * 
 * R2DBC Auditing:
 * - @CreatedDate: Automatically set on insert
 * - @LastModifiedDate: Automatically updated on save
 * - Requires @EnableR2dbcAuditing in configuration
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("customers")
public class CustomerEntity {
    
    @Id
    private Long id;
    
    @Column("first_name")
    private String firstName;
    
    @Column("last_name")
    private String lastName;
    
    @Column("email")
    private String email;
    
    @Column("phone")
    private String phone;
    
    @Column("address")
    private String address;
    
    @Column("city")
    private String city;
    
    @Column("state")
    private String state;
    
    @Column("zip_code")
    private String zipCode;
    
    @Column("country")
    private String country;
    
    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}

// Made with Bob
