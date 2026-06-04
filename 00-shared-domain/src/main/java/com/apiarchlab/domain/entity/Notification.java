package com.apiarchlab.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.apiarchlab.domain.enums.NotificationStatus;
import com.apiarchlab.domain.enums.NotificationType;
import jakarta.validation.constraints.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    private Long id;

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    private Long orderId;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    private NotificationStatus status;

    @NotBlank(message = "Channel is required")
    private String channel; // EMAIL, SMS, PUSH, IN_APP

    @NotBlank(message = "Subject is required")
    private String subject;

    @NotBlank(message = "Message is required")
    private String message;

    private String recipient; // email address, phone number, etc.

    private Integer retryCount;
    private LocalDateTime nextRetryAt;

    private LocalDateTime createdAt;
    private LocalDateTime sentAt;
    private LocalDateTime deliveredAt;

    private String errorMessage;
}

