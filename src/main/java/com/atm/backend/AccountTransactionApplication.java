package com.atm.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Entry point for the Account Transaction Management System.
 *
 * @EnableRetry activates Spring Retry support used by TransactionServiceImpl
 * to silently retry deposit operations on Optimistic Locking conflicts.
 */
@SpringBootApplication
@EnableRetry
public class AccountTransactionApplication {

    public static void main(String[] args) {
        SpringApplication.run(AccountTransactionApplication.class, args);
    }
}
