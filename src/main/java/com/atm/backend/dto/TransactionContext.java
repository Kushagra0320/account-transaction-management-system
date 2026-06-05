package com.atm.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Internal context object carrying all inputs needed by a TransactionStrategy. */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionContext {

    private BigDecimal amount;
    private String description;
    /** Only used by TransferStrategy — null for deposit/withdraw. */
    private String destinationAccountNumber;
}
