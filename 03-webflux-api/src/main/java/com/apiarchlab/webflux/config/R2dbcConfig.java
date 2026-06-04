package com.apiarchlab.webflux.config;

import io.r2dbc.spi.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

/**
 * R2DBC Configuration for Reactive Database Access
 * 
 * R2DBC (Reactive Relational Database Connectivity) is the reactive alternative to JDBC.
 * 
 * Key Differences from JDBC:
 * - Non-blocking I/O operations
 * - Returns Mono/Flux instead of blocking results
 * - Supports backpressure
 * - Event-driven architecture
 * - Better resource utilization
 * 
 * Features:
 * - Connection pooling
 * - Database initialization with schema.sql
 * - Auditing support (@CreatedDate, @LastModifiedDate)
 */
@Configuration
@EnableR2dbcAuditing
@Slf4j
public class R2dbcConfig {

    /**
     * Initialize database schema on startup
     * 
     * This bean creates tables if they don't exist.
     * In production, use Flyway or Liquibase for migrations.
     */
    @Bean
    public ConnectionFactoryInitializer initializer(ConnectionFactory connectionFactory) {
        log.info("Initializing R2DBC database schema...");
        
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        
        // Execute schema.sql to create tables
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        
        initializer.setDatabasePopulator(populator);
        
        log.info("R2DBC database schema initialized successfully");
        return initializer;
    }
}

// Made with Bob
