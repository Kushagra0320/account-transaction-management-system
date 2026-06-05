package com.atm.backend.service.impl;

import com.atm.backend.dto.*;
import com.atm.backend.entity.Account;
import com.atm.backend.entity.Transaction;
import com.atm.backend.exception.AccountNotFoundException;
import com.atm.backend.repository.AccountRepository;
import com.atm.backend.repository.TransactionRepository;
import com.atm.backend.service.TransactionService;
import com.atm.backend.service.strategy.DepositStrategy;
import com.atm.backend.service.strategy.WithdrawStrategy;
import com.atm.backend.service.strategy.TransferStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Core implementation of TransactionService.
 *
 * Design Decisions:
 * - @Transactional(rollbackFor = Exception.class) ensures any failure triggers full DB rollback (ACID).
 * - Deposits use @Retryable on OptimisticLockingFailureException — up to 3 silent retries.
 * - Withdrawals and transfers fail fast with 409 Conflict — user must be aware and retry manually.
 * - Idempotency check is delegated to IdempotencyServiceImpl (REQUIRES_NEW propagation).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyServiceImpl idempotencyService;
    private final DepositStrategy depositStrategy;
    private final WithdrawStrategy withdrawStrategy;
    private final TransferStrategy transferStrategy;

    @Retryable(
        retryFor = ObjectOptimisticLockingFailureException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 100)
    )
    @Transactional(rollbackFor = Exception.class)
    @Override
    public TransactionResponse deposit(String idempotencyKey, SingleAccountTransactionRequest request) {
        Optional<TransactionResponse> cached = idempotencyService.findCachedResponse(idempotencyKey);
        if (cached.isPresent()) return cached.get();

        Account account = findAccountOrThrow(request.getAccountNumber());
        TransactionContext context = TransactionContext.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        TransactionResponse response = depositStrategy.execute(account, context);
        idempotencyService.saveRecord(idempotencyKey, response, 200);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TransactionResponse withdraw(String idempotencyKey, SingleAccountTransactionRequest request) {
        Optional<TransactionResponse> cached = idempotencyService.findCachedResponse(idempotencyKey);
        if (cached.isPresent()) return cached.get();

        Account account = findAccountOrThrow(request.getAccountNumber());
        TransactionContext context = TransactionContext.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();

        TransactionResponse response = withdrawStrategy.execute(account, context);
        idempotencyService.saveRecord(idempotencyKey, response, 200);
        return response;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public TransactionResponse transfer(String idempotencyKey, TransferRequest request) {
        Optional<TransactionResponse> cached = idempotencyService.findCachedResponse(idempotencyKey);
        if (cached.isPresent()) return cached.get();

        if (request.getSourceAccountNumber().equals(request.getDestinationAccountNumber())) {
            throw new IllegalArgumentException("Cannot transfer funds to the same account.");
        }

        Account sourceAccount = findAccountOrThrow(request.getSourceAccountNumber());
        TransactionContext context = TransactionContext.builder()
                .amount(request.getAmount())
                .description(request.getDescription())
                .destinationAccountNumber(request.getDestinationAccountNumber())
                .build();

        TransactionResponse response = transferStrategy.execute(sourceAccount, context);
        idempotencyService.saveRecord(idempotencyKey, response, 200);
        return response;
    }

    @Transactional(readOnly = true)
    @Override
    public List<TransactionHistoryResponse> getTransactionHistory(String accountNumber, LocalDate startDate, LocalDate endDate) {
        Account account = findAccountOrThrow(accountNumber);

        List<Transaction> transactions;
        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("startDate must not be after endDate.");
            }
            LocalDateTime start = startDate.atStartOfDay();
            LocalDateTime end = endDate.atTime(LocalTime.MAX);
            transactions = transactionRepository.findByAccountIdAndDateRange(account.getId(), start, end);
        } else {
            transactions = transactionRepository.findByAccountIdOrderByTransactionDateDesc(account.getId());
        }

        return transactions.stream().map(txn -> TransactionHistoryResponse.builder()
                .transactionRef(txn.getTransactionRef())
                .type(txn.getType().name())
                .amount(txn.getAmount())
                .balanceBefore(txn.getBalanceBefore())
                .balanceAfter(txn.getBalanceAfter())
                .referenceId(txn.getReferenceId())
                .description(txn.getDescription())
                .transactionDate(txn.getTransactionDate())
                .build()
        ).collect(Collectors.toList());
    }

    private Account findAccountOrThrow(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));
    }
}
