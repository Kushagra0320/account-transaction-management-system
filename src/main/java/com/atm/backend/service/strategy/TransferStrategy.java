package com.atm.backend.service.strategy;

import com.atm.backend.dto.TransactionContext;
import com.atm.backend.dto.TransactionResponse;
import com.atm.backend.entity.Account;
import com.atm.backend.entity.AccountStatus;
import com.atm.backend.entity.Transaction;
import com.atm.backend.entity.TransactionType;
import com.atm.backend.exception.AccountNotActiveException;
import com.atm.backend.exception.AccountNotFoundException;
import com.atm.backend.exception.InsufficientBalanceException;
import com.atm.backend.repository.AccountRepository;
import com.atm.backend.repository.TransactionRepository;
import com.atm.backend.util.IdGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy: TRANSFER (two-legged atomic operation).
 * Business Rules:
 * 1. Source account must be ACTIVE.
 * 2. Source must have sufficient balance.
 * 3. Destination account must be ACTIVE.
 * The @Transactional boundary is held by TransactionServiceImpl —
 * if either leg fails, both are rolled back.
 */
@Component
@RequiredArgsConstructor
public class TransferStrategy implements TransactionStrategy {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    @Override
    public TransactionResponse execute(Account sourceAccount, TransactionContext context) {
        BigDecimal amount = context.getAmount();

        if (!AccountStatus.ACTIVE.equals(sourceAccount.getStatus())) {
            throw new AccountNotActiveException("Source account " + sourceAccount.getAccountNumber() +
                    " is " + sourceAccount.getStatus() + ". Only ACTIVE accounts can initiate transfers.");
        }
        if (sourceAccount.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient balance in source account. Available: " +
                    sourceAccount.getBalance() + ", Requested: " + amount);
        }

        Account destinationAccount = accountRepository.findByAccountNumber(context.getDestinationAccountNumber())
                .orElseThrow(() -> new AccountNotFoundException(context.getDestinationAccountNumber()));

        if (!AccountStatus.ACTIVE.equals(destinationAccount.getStatus())) {
            throw new AccountNotActiveException("Destination account " + destinationAccount.getAccountNumber() +
                    " is " + destinationAccount.getStatus() + ". Cannot transfer to a non-ACTIVE account.");
        }

        String transferRef = IdGenerator.generateRef();

        // Leg 1: Debit source
        BigDecimal sourceBalanceBefore = sourceAccount.getBalance();
        BigDecimal sourceBalanceAfter = sourceBalanceBefore.subtract(amount);
        sourceAccount.setBalance(sourceBalanceAfter);
        accountRepository.save(sourceAccount);

        Transaction debitTxn = buildTxn(sourceAccount, TransactionType.TRANSFER_OUT,
                amount, sourceBalanceBefore, sourceBalanceAfter, transferRef, context.getDescription());
        transactionRepository.save(debitTxn);

        // Leg 2: Credit destination
        BigDecimal destBalanceBefore = destinationAccount.getBalance();
        BigDecimal destBalanceAfter = destBalanceBefore.add(amount);
        destinationAccount.setBalance(destBalanceAfter);
        accountRepository.save(destinationAccount);

        Transaction creditTxn = buildTxn(destinationAccount, TransactionType.TRANSFER_IN,
                amount, destBalanceBefore, destBalanceAfter, transferRef, context.getDescription());
        transactionRepository.save(creditTxn);

        return TransactionResponse.builder()
                .transactionRef(transferRef)
                .accountNumber(sourceAccount.getAccountNumber())
                .type(TransactionType.TRANSFER_OUT.name())
                .amount(amount)
                .balanceBefore(sourceBalanceBefore)
                .balanceAfter(sourceBalanceAfter)
                .transactionDate(debitTxn.getTransactionDate())
                .description(context.getDescription())
                .message("Transfer to " + destinationAccount.getAccountNumber() + " successful")
                .build();
    }

    private Transaction buildTxn(Account account, TransactionType type, BigDecimal amount,
            BigDecimal before, BigDecimal after, String referenceId, String description) {
        Transaction txn = new Transaction();
        txn.setTransactionRef(IdGenerator.generateRef());
        txn.setAccount(account);
        txn.setType(type);
        txn.setAmount(amount);
        txn.setBalanceBefore(before);
        txn.setBalanceAfter(after);
        txn.setReferenceId(referenceId);
        txn.setDescription(description);
        return txn;
    }
}
