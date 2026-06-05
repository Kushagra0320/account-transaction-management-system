package com.atm.backend.service;

import com.atm.backend.dto.SingleAccountTransactionRequest;
import com.atm.backend.dto.TransactionHistoryResponse;
import com.atm.backend.dto.TransactionResponse;
import com.atm.backend.dto.TransferRequest;

import java.time.LocalDate;
import java.util.List;

/**
 * Service interface for all transaction operations.
 * The controller depends on this interface, not the implementation —
 * following Dependency Inversion Principle (DIP).
 */
public interface TransactionService {
    TransactionResponse deposit(String idempotencyKey, SingleAccountTransactionRequest request);
    TransactionResponse withdraw(String idempotencyKey, SingleAccountTransactionRequest request);
    TransactionResponse transfer(String idempotencyKey, TransferRequest request);
    List<TransactionHistoryResponse> getTransactionHistory(String accountNumber, LocalDate startDate, LocalDate endDate);
}
