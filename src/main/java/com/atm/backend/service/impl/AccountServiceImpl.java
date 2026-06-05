package com.atm.backend.service.impl;

import com.atm.backend.dto.AccountResponse;
import com.atm.backend.entity.Account;
import com.atm.backend.exception.AccountNotFoundException;
import com.atm.backend.repository.AccountRepository;
import com.atm.backend.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Account lookup service — returns safe AccountResponse DTOs to the controller.
 * Keeps personal data (customer name, email) inside the response for convenience
 * without ever leaking the internal database ID.
 */
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    @Override
    public AccountResponse getAccount(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException(accountNumber));

        return AccountResponse.builder()
                .accountNumber(account.getAccountNumber())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .customerFirstName(account.getCustomer().getFirstName())
                .customerLastName(account.getCustomer().getLastName())
                .customerEmail(account.getCustomer().getEmail())
                .customerPhoneNumber(account.getCustomer().getPhoneNumber())
                .build();
    }
}
