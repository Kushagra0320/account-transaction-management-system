package com.atm.backend.service.strategy;

import com.atm.backend.dto.TransactionContext;
import com.atm.backend.dto.TransactionResponse;
import com.atm.backend.entity.Account;

/**
 * Strategy interface for all transaction types.
 *
 * Design Pattern: Strategy (Behavioral)
 * - Encapsulates each transaction algorithm behind a common interface.
 * - Keeps TransactionServiceImpl closed for modification when new types are added (OCP).
 * - TransactionContext groups all inputs; strategies read only what they need.
 */
public interface TransactionStrategy {
    TransactionResponse execute(Account account, TransactionContext context);
}
