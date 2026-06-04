package com.apiarchlab.webflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;

/**
 * Main application class for WebFlux Reactive API
 * 
 * Key Differences from Spring MVC:
 * - Uses Netty server instead of Tomcat
 * - Non-blocking I/O throughout the stack
 * - Reactive streams with Mono and Flux
 * - R2DBC for reactive database access
 * - Event-loop based threading model
 * 
 * Architecture:
 * Client → WebFlux Controller → Reactive Service → R2DBC Repository → Database
 * 
 * All operations return Mono<T> (single value) or Flux<T> (multiple values)
 * No blocking operations allowed in the reactive chain
 */
@SpringBootApplication
@EnableR2dbcRepositories
public class WebFluxApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebFluxApplication.class, args);
        System.out.println("""
            
            ╔══════════════════════════════════════════════════════════════╗
            ║                                                              ║
            ║   🚀 WebFlux Reactive API Started Successfully!             ║
            ║                                                              ║
            ║   Server: Netty (Non-blocking)                              ║
            ║   Port: 8082                                                 ║
            ║   Base URL: http://localhost:8082/api                       ║
            ║                                                              ║
            ║   Endpoints:                                                 ║
            ║   • GET    /api/customers                                    ║
            ║   • GET    /api/customers/{id}                               ║
            ║   • POST   /api/customers                                    ║
            ║   • GET    /api/customers/stream (SSE)                       ║
            ║                                                              ║
            ║   • GET    /api/orders                                       ║
            ║   • GET    /api/orders/{id}                                  ║
            ║   • POST   /api/orders                                       ║
            ║   • GET    /api/orders/stream (SSE)                          ║
            ║                                                              ║
            ║   Functional Endpoints:                                      ║
            ║   • GET    /functional/customers                             ║
            ║   • GET    /functional/customers/{id}                        ║
            ║   • POST   /functional/customers                             ║
            ║                                                              ║
            ║   Features:                                                  ║
            ║   ✓ Fully non-blocking reactive stack                       ║
            ║   ✓ R2DBC reactive database access                          ║
            ║   ✓ Server-Sent Events (SSE) streaming                      ║
            ║   ✓ Backpressure support                                    ║
            ║   ✓ Functional routing                                      ║
            ║                                                              ║
            ╚══════════════════════════════════════════════════════════════╝
            """);
    }
}

// Made with Bob
