package com.atm.backend.service;

import com.atm.backend.dto.AccountResponse;

/**
 * Service interface for account lookup operations.
 * Separate from TransactionService for single-responsibility.
 */
public interface AccountService {
    AccountResponse getAccount(String accountNumber);
}
