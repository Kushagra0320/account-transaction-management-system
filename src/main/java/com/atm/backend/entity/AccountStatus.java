package com.atm.backend.entity;

/**
 * Represents the status of a bank account.
 * ACTIVE   — normal operations allowed (deposit, withdraw, transfer)
 * FROZEN   — can receive funds but cannot withdraw or transfer out
 * INACTIVE — fully closed; no transactions allowed
 */
public enum AccountStatus {
    ACTIVE,
    FROZEN,
    INACTIVE
}
