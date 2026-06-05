package com.atm.backend.service.impl;

import com.atm.backend.dto.TransactionResponse;
import com.atm.backend.entity.IdempotencyRecord;
import com.atm.backend.repository.IdempotencyRecordRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Manages idempotency key lifecycle.
 *
 * Why REQUIRES_NEW?
 * Running in the caller's @Transactional would mean a rollback in the caller
 * also rolls back the idempotency record — causing the next retry to re-execute
 * an already processed transaction. REQUIRES_NEW ensures the record row is
 * committed independently regardless of the parent transaction outcome.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IdempotencyServiceImpl {

    private final IdempotencyRecordRepository idempotencyRecordRepository;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public Optional<TransactionResponse> findCachedResponse(String idempotencyKey) {
        return idempotencyRecordRepository.findByIdempotencyKey(idempotencyKey)
                .map(record -> {
                    try {
                        log.info("Idempotency cache hit for key: {}", idempotencyKey);
                        return objectMapper.readValue(record.getResponseBody(), TransactionResponse.class);
                    } catch (Exception e) {
                        log.error("Failed to deserialize cached idempotency response for key: {}", idempotencyKey, e);
                        return null;
                    }
                });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRecord(String idempotencyKey, TransactionResponse response, int httpStatus) {
        try {
            IdempotencyRecord record = new IdempotencyRecord();
            record.setIdempotencyKey(idempotencyKey);
            record.setResponseBody(objectMapper.writeValueAsString(response));
            record.setHttpStatus(httpStatus);
            idempotencyRecordRepository.save(record);
            log.info("Idempotency key saved: {}", idempotencyKey);
        } catch (Exception e) {
            log.warn("Could not persist idempotency record for key: {}. Proceeding.", idempotencyKey);
        }
    }
}
