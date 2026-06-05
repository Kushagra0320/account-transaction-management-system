package com.atm.backend.service;

import com.atm.backend.dto.*;
import com.atm.backend.entity.*;
import com.atm.backend.exception.AccountNotActiveException;
import com.atm.backend.exception.InsufficientBalanceException;
import com.atm.backend.repository.AccountRepository;
import com.atm.backend.repository.IdempotencyRecordRepository;
import com.atm.backend.repository.TransactionRepository;
import com.atm.backend.service.impl.IdempotencyServiceImpl;
import com.atm.backend.service.impl.TransactionServiceImpl;
import com.atm.backend.service.strategy.DepositStrategy;
import com.atm.backend.service.strategy.WithdrawStrategy;
import com.atm.backend.service.strategy.TransferStrategy;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration Test Suite for TransactionServiceImpl.
 *
 * Uses H2 in-memory database (configured in test/resources/application.properties).
 * The @Transactional annotation rolls back each test, keeping tests independent.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TransactionServiceIntegrationTest {

    @Autowired private TransactionServiceImpl transactionService;
    @Autowired private AccountRepository accountRepository;
    @Autowired private TransactionRepository transactionRepository;
    @Autowired private IdempotencyRecordRepository idempotencyRecordRepository;
    @Autowired private com.atm.backend.repository.CustomerRepository customerRepository;

    private Account testAccount;
    private Account secondAccount;

    @BeforeEach
    void setUp() {
        Customer customer = new Customer();
        customer.setFirstName("Test");
        customer.setLastName("User");
        customer.setEmail("t" + UUID.randomUUID().toString().replace("-", "").substring(0, 10) + "@example.com");
        customer = customerRepository.save(customer); // must persist Customer first — Account has no CascadeType.PERSIST

        testAccount = new Account();
        testAccount.setAccountNumber(UUID.randomUUID().toString()); // UUID is exactly 36 chars — fits the column limit
        testAccount.setCustomer(customer);
        testAccount.setBalance(new BigDecimal("10000.00"));
        testAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(testAccount);

        secondAccount = new Account();
        secondAccount.setAccountNumber(UUID.randomUUID().toString()); // UUID is exactly 36 chars — fits the column limit
        secondAccount.setCustomer(customer);
        secondAccount.setBalance(new BigDecimal("5000.00"));
        secondAccount.setStatus(AccountStatus.ACTIVE);
        accountRepository.save(secondAccount);
    }

    @Test
    void deposit_shouldIncreaseBalance() {
        SingleAccountTransactionRequest req = new SingleAccountTransactionRequest();
        req.setAccountNumber(testAccount.getAccountNumber());
        req.setAmount(new BigDecimal("500.00"));

        TransactionResponse response = transactionService.deposit(UUID.randomUUID().toString(), req);

        assertThat(response.getBalanceAfter()).isEqualByComparingTo("10500.00");
        assertThat(response.getType()).isEqualTo("DEPOSIT");
    }

    @Test
    void withdraw_shouldDecreaseBalance() {
        SingleAccountTransactionRequest req = new SingleAccountTransactionRequest();
        req.setAccountNumber(testAccount.getAccountNumber());
        req.setAmount(new BigDecimal("1000.00"));

        TransactionResponse response = transactionService.withdraw(UUID.randomUUID().toString(), req);

        assertThat(response.getBalanceAfter()).isEqualByComparingTo("9000.00");
        assertThat(response.getType()).isEqualTo("WITHDRAW");
    }

    @Test
    void withdraw_insufficientBalance_shouldThrow() {
        SingleAccountTransactionRequest req = new SingleAccountTransactionRequest();
        req.setAccountNumber(testAccount.getAccountNumber());
        req.setAmount(new BigDecimal("99999.00"));

        assertThatThrownBy(() -> transactionService.withdraw(UUID.randomUUID().toString(), req))
                .isInstanceOf(InsufficientBalanceException.class);
    }

    @Test
    void transfer_shouldMoveMoneyBetweenAccounts() {
        TransferRequest req = new TransferRequest();
        req.setSourceAccountNumber(testAccount.getAccountNumber());
        req.setDestinationAccountNumber(secondAccount.getAccountNumber());
        req.setAmount(new BigDecimal("2000.00"));

        TransactionResponse response = transactionService.transfer(UUID.randomUUID().toString(), req);

        assertThat(response.getBalanceAfter()).isEqualByComparingTo("8000.00");

        Account refreshed = accountRepository.findByAccountNumber(secondAccount.getAccountNumber()).orElseThrow();
        assertThat(refreshed.getBalance()).isEqualByComparingTo("7000.00");
    }

    @Test
    void deposit_withSameIdempotencyKey_shouldNotDoubleDeposit() {
        String key = UUID.randomUUID().toString();
        SingleAccountTransactionRequest req = new SingleAccountTransactionRequest();
        req.setAccountNumber(testAccount.getAccountNumber());
        req.setAmount(new BigDecimal("100.00"));

        TransactionResponse first  = transactionService.deposit(key, req);
        TransactionResponse second = transactionService.deposit(key, req);

        assertThat(first.getTransactionRef()).isEqualTo(second.getTransactionRef());
        assertThat(transactionRepository.findByAccountIdOrderByTransactionDateDesc(testAccount.getId())).hasSize(1);
    }

    @Test
    void withdraw_fromFrozenAccount_shouldThrow() {
        testAccount.setStatus(AccountStatus.FROZEN);
        accountRepository.save(testAccount);

        SingleAccountTransactionRequest req = new SingleAccountTransactionRequest();
        req.setAccountNumber(testAccount.getAccountNumber());
        req.setAmount(new BigDecimal("100.00"));

        assertThatThrownBy(() -> transactionService.withdraw(UUID.randomUUID().toString(), req))
                .isInstanceOf(AccountNotActiveException.class);
    }
}
