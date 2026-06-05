package com.atm.backend.controller;

import com.atm.backend.dto.AccountResponse;
import com.atm.backend.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller — Account Lookup.
 * Provides a safe endpoint for the frontend to retrieve account details
 * by account number (never by internal DB ID).
 */
@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account Management", description = "Account lookup operations")
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "Get account details by account number",
               description = "Returns account info and embedded customer details. Safe — internal DB IDs are never exposed.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account found"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable String accountNumber) {
        log.info("Account lookup | AccountNumber: {}", accountNumber);
        return ResponseEntity.ok(accountService.getAccount(accountNumber));
    }
}
