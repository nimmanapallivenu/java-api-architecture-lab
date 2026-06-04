package com.apiarchlab.restclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * REST API with Async WebClient for non-blocking downstream calls.
 * 
 * Key Features:
 * - Spring MVC (blocking HTTP endpoints)
 * - WebClient for async calls to downstream services
 * - CompletableFuture for async processing
 * - @Async for background task execution
 * - Reactor/Netty for non-blocking I/O
 * 
 * Benefits over Module 01:
 * - Better thread utilization (threads not waiting on I/O)
 * - Faster response times (don't wait for external services)
 * - Better scalability (can handle more concurrent users)
 * 
 * Use Case:
 * When your API calls external services and you want non-blocking I/O
 * to reduce thread consumption and improve throughput.
 */
@SpringBootApplication
@EnableAsync
public class RestClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(RestClientApplication.class, args);
    }
}

