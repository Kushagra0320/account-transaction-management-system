package com.atm.backend.service.strategy;

import com.atm.backend.dto.TransactionContext;
import com.atm.backend.dto.TransactionResponse;
import com.atm.backend.entity.Account;
import com.atm.backend.entity.AccountStatus;
import com.atm.backend.entity.Transaction;
import com.atm.backend.entity.TransactionType;
import com.atm.backend.exception.AccountNotActiveException;
import com.atm.backend.exception.InsufficientBalanceException;
import com.atm.backend.repository.AccountRepository;
import com.atm.backend.repository.TransactionRepository;
import com.atm.backend.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy: WITHDRAW
 * Business Rules:
 * 1. Account must be ACTIVE.
 * 2. Available balance must be >= withdrawal amount.
 */
@Component
@RequiredArgsConstructor
public class WithdrawStrategy implements TransactionStrategy {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public TransactionResponse execute(Account account, TransactionContext context) {
        BigDecimal amount = context.getAmount();

        if (!AccountStatus.ACTIVE.equals(account.getStatus())) {
            throw new AccountNotActiveException("Account " + account.getAccountNumber() +
                    " is " + account.getStatus() + ". Only ACTIVE accounts can withdraw.");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance. Available: " +
                    account.getBalance() + ", Requested: " + amount);
        }

        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.subtract(amount);

        account.setBalance(balanceAfter);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setTransactionRef(IdGenerator.generateRef());
        transaction.setAccount(account);
        transaction.setType(TransactionType.WITHDRAW);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(context.getDescription());
        transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .transactionRef(transaction.getTransactionRef())
                .accountNumber(account.getAccountNumber())
                .type(TransactionType.WITHDRAW.name())
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .transactionDate(transaction.getTransactionDate())
                .description(context.getDescription())
                .message("Withdrawal successful")
                .build();
    }
}
