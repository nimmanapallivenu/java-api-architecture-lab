package com.apiarchlab.restclient.client;

import com.apiarchlab.restclient.dto.PaymentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

/**
 * Async client for external Payment Service.
 * Uses WebClient for non-blocking HTTP calls.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceClient {
    private final WebClient webClient;

    @Value("${external.services.payment-service.url:http://localhost:9090}")
    private String paymentServiceUrl;

    @Value("${external.services.payment-service.timeout:5000ms}")
    private Duration timeout;

    /**
     * Process payment asynchronously.
     * Returns CompletableFuture for blocking call sites.
     */
    public CompletableFuture<PaymentDto> processPaymentAsync(Long orderId, PaymentDto paymentRequest) {
        log.debug("Calling payment service asynchronously for order: {}", orderId);

        return webClient.post()
                .uri(paymentServiceUrl + "/api/process-payment")
                .bodyValue(paymentRequest)
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .timeout(timeout)
                .doOnError(error -> log.error("Payment service call failed for order: {}, error: {}", orderId, error.getMessage()))
                .doOnNext(result -> log.info("Payment processed successfully for order: {}", orderId))
                .subscribeOn(Schedulers.boundedElastic())  // Non-blocking execution
                .toFuture()
                .exceptionally(ex -> {
                    log.warn("Payment processing failed, returning default payment status for order: {}", orderId);
                    return PaymentDto.builder()
                            .orderId(orderId)
                            .status(com.apiarchlab.restclient.enums.PaymentStatus.FAILED)
                            .errorMessage(ex.getMessage())
                            .build();
                });
    }

    /**
     * Alternative Mono-based method for pure reactive code.
     */
    public Mono<PaymentDto> processPaymentReactive(Long orderId, PaymentDto paymentRequest) {
        log.debug("Calling payment service (reactive) for order: {}", orderId);

        return webClient.post()
                .uri(paymentServiceUrl + "/api/process-payment")
                .bodyValue(paymentRequest)
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .timeout(timeout)
                .doOnError(error -> log.error("Payment service call failed for order: {}", orderId))
                .onErrorResume(error -> Mono.just(
                        PaymentDto.builder()
                                .orderId(orderId)
                                .status(com.apiarchlab.restclient.enums.PaymentStatus.FAILED)
                                .errorMessage(error.getMessage())
                                .build()
                ));
    }

    /**
     * Query payment status asynchronously.
     */
    public CompletableFuture<PaymentDto> getPaymentStatusAsync(String transactionId) {
        log.debug("Querying payment status for transaction: {}", transactionId);

        return webClient.get()
                .uri(paymentServiceUrl + "/api/payments/{transactionId}", transactionId)
                .retrieve()
                .bodyToMono(PaymentDto.class)
                .timeout(timeout)
                .subscribeOn(Schedulers.boundedElastic())
                .toFuture()
                .exceptionally(ex -> {
                    log.error("Failed to get payment status for transaction: {}", transactionId);
                    return null;
                });
    }
}

