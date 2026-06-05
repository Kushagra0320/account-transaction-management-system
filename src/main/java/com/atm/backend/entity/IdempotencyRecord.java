package com.atm.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Stores processed Idempotency-Keys to prevent processing the same
 * transaction request more than once (client retries, double-clicks, etc).
 *
 * Workflow:
 * 1. Service checks if the incoming Idempotency-Key already has a record here.
 * 2. If YES: Return the stored response immediately — no DB modification.
 * 3. If NO:  Process the transaction and store the response here afterwards.
 */
@Entity
@Table(name = "idempotency_record")
@Getter
@Setter
@NoArgsConstructor
public class IdempotencyRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idempotency_seq")
    @SequenceGenerator(name = "idempotency_seq", sequenceName = "idempotency_seq", allocationSize = 1, initialValue = 11)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, unique = true, length = 36)
    private String idempotencyKey;

    /** Serialized JSON of the original successful transaction response. */
    @Column(name = "response_body", nullable = false, columnDefinition = "CLOB")
    private String responseBody;

    @Column(name = "http_status", nullable = false)
    private Integer httpStatus;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
