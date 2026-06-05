package com.atm.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Safe response DTO for account lookup.
 * Exposes account_number to the API — NOT the internal database PK (id).
 * Customer's personal info is embedded to avoid a separate API call from the frontend.
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponse {

    private String accountNumber;
    private BigDecimal balance;
    private String status;
    private LocalDateTime createdAt;

    // Embedded customer details
    private String customerFirstName;
    private String customerLastName;
    private String customerEmail;
    private String customerPhoneNumber;
}
