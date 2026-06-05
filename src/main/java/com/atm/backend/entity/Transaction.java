package com.atm.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a single financial transaction event.
 *
 * Design Decisions:
 * - balance_before and balance_after provide a complete immutable audit trail.
 * - reference_id links the two rows of a Transfer (TRANSFER_OUT + TRANSFER_IN).
 * - transaction_ref (UUID) is the external identifier exposed to the API.
 * - No @Version — Transaction records are append-only; never updated.
 */
@Entity
@Table(name = "transaction",
    indexes = {
        @Index(name = "idx_txn_account_date", columnList = "account_id, transaction_date")
    })
@Getter
@Setter
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
    @SequenceGenerator(name = "transaction_seq", sequenceName = "transaction_seq", allocationSize = 1, initialValue = 11)
    private Long id;

    @Column(name = "transaction_ref", nullable = false, unique = true, length = 36)
    private String transactionRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(name = "balance_before", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceBefore;

    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    /** Links the TRANSFER_OUT and TRANSFER_IN records of a single transfer. */
    @Column(name = "reference_id", length = 36)
    private String referenceId;

    @Column(length = 255)
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @PrePersist
    private void onCreate() {
        this.transactionDate = LocalDateTime.now();
    }
}
