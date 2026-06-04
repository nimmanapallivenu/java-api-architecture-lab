package com.apiarchlab.restclient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentRequest {
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    @NotBlank(message = "Card token is required")
    private String cardToken;

    private String cardHolderName;
}

