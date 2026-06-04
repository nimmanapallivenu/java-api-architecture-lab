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
public class CreateCustomerRequest {
    @NotBlank(message = "First name is required")
    private String firstName;

    @NotBlank(message = "Last name is required")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[+]?[0-9]{10,}$", message = "Phone number should be valid")
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    private String city;
    private String state;

    @Pattern(regexp = "^[0-9]{5,6}$", message = "Zip code should be valid")
    private String zipCode;

    private String country;
}

