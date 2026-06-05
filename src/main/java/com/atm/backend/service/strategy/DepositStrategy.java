package com.atm.backend.service.strategy;

import com.atm.backend.dto.TransactionContext;
import com.atm.backend.dto.TransactionResponse;
import com.atm.backend.entity.Account;
import com.atm.backend.entity.Transaction;
import com.atm.backend.entity.TransactionType;
import com.atm.backend.repository.AccountRepository;
import com.atm.backend.repository.TransactionRepository;
import com.atm.backend.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy: DEPOSIT
 * Business Rule: Amount must be > 0 (enforced via @Valid on DTO).
 * Any account status can receive a deposit (only withdrawals require ACTIVE).
 */
@Component
@RequiredArgsConstructor
public class DepositStrategy implements TransactionStrategy {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public TransactionResponse execute(Account account, TransactionContext context) {
        BigDecimal amount = context.getAmount();
        BigDecimal balanceBefore = account.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);

        account.setBalance(balanceAfter);
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setTransactionRef(IdGenerator.generateRef());
        transaction.setAccount(account);
        transaction.setType(TransactionType.DEPOSIT);
        transaction.setAmount(amount);
        transaction.setBalanceBefore(balanceBefore);
        transaction.setBalanceAfter(balanceAfter);
        transaction.setDescription(context.getDescription());
        transactionRepository.save(transaction);

        return TransactionResponse.builder()
                .transactionRef(transaction.getTransactionRef())
                .accountNumber(account.getAccountNumber())
                .type(TransactionType.DEPOSIT.name())
                .amount(amount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .transactionDate(transaction.getTransactionDate())
                .description(context.getDescription())
                .message("Deposit successful")
                .build();
    }
}
