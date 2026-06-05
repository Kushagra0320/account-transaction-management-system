package com.atm.backend.entity;

/**
 * Represents the type of a financial transaction.
 * DEPOSIT       — money coming into an account from external source
 * WITHDRAW      — money going out of an account to the customer
 * TRANSFER_IN   — credit leg of an internal account-to-account transfer
 * TRANSFER_OUT  — debit leg of an internal account-to-account transfer
 */
public enum TransactionType {
    DEPOSIT,
    WITHDRAW,
    TRANSFER_IN,
    TRANSFER_OUT
}
