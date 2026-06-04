package com.apiarchlab.restclient.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * Async client for external Notification Service.
 * Fire-and-forget pattern for notifications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceClient {
    private final WebClient webClient;

    @Value("${external.services.notification-service.url:http://localhost:9091}")
    private String notificationServiceUrl;

    @Value("${external.services.notification-service.timeout:3000ms}")
    private Duration timeout;

    /**
     * Send notification asynchronously (fire and forget).
     * Does not block the main request.
     */
    public void sendNotificationAsync(String recipientEmail, String subject, String message) {
        log.debug("Sending notification asynchronously to: {}", recipientEmail);

        Map<String, String> notification = new HashMap<>();
        notification.put("recipientEmail", recipientEmail);
        notification.put("subject", subject);
        notification.put("message", message);

        webClient.post()
                .uri(notificationServiceUrl + "/api/send")
                .bodyValue(notification)
                .retrieve()
                .toBodilessEntity()
                .timeout(timeout)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnError(error -> log.warn("Failed to send notification to: {}, error: {}", recipientEmail, error.getMessage()))
                .doOnNext(response -> log.info("Notification sent successfully to: {}", recipientEmail))
                .subscribe();  // Fire and forget - no waiting
    }

    /**
     * Send order update notification asynchronously.
     */
    public CompletableFuture<Void> sendOrderUpdateNotification(Long orderId, String status, String recipientEmail) {
        log.debug("Sending order update notification for order: {} with status: {}", orderId, status);

        Map<String, Object> notification = new HashMap<>();
        notification.put("orderId", orderId);
        notification.put("status", status);
        notification.put("recipientEmail", recipientEmail);

        return webClient.post()
                .uri(notificationServiceUrl + "/api/order-update")
                .bodyValue(notification)
                .retrieve()
                .toBodilessEntity()
                .timeout(timeout)
                .subscribeOn(Schedulers.boundedElastic())
                .toFuture()
                .thenApply(response -> {
                    log.info("Order update notification sent for order: {}", orderId);
                    return null;
                })
                .exceptionally(ex -> {
                    log.warn("Failed to send order notification for order: {}", orderId);
                    return null;  // Don't fail main request
                });
    }

    /**
     * Send payment confirmation notification.
     */
    public void sendPaymentConfirmation(String transactionId, Double amount, String recipientEmail) {
        log.debug("Sending payment confirmation to: {}", recipientEmail);

        Map<String, Object> notification = new HashMap<>();
        notification.put("transactionId", transactionId);
        notification.put("amount", amount);
        notification.put("recipientEmail", recipientEmail);

        webClient.post()
                .uri(notificationServiceUrl + "/api/payment-confirmation")
                .bodyValue(notification)
                .retrieve()
                .toBodilessEntity()
                .timeout(timeout)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                    response -> log.info("Payment confirmation sent to: {}", recipientEmail),
                    error -> log.warn("Failed to send payment confirmation to: {}", recipientEmail)
                );
    }
}

