package com.atm.backend.controller;

import com.atm.backend.dto.TransactionHistoryResponse;
import com.atm.backend.dto.SingleAccountTransactionRequest;
import com.atm.backend.dto.TransactionResponse;
import com.atm.backend.dto.TransferRequest;
import com.atm.backend.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST Controller — Transaction Management.
 *
 * Intentionally thin: handles HTTP concerns only.
 * All business logic is delegated to TransactionService.
 * Idempotency-Key is required as an HTTP header on all POST endpoints.
 */
@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transaction Management", description = "Deposit, withdrawal, transfer operations and history")
@SecurityRequirement(name = "basicAuth")
public class TransactionController {

    private final TransactionService transactionService;

    @Operation(summary = "Deposit funds into an account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Deposit successful"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Concurrent transaction conflict")
    })
    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @RequestHeader("Idempotency-Key")
            @Parameter(description = "Unique UUID to prevent duplicate processing", required = true)
            String idempotencyKey,
            @Valid @RequestBody SingleAccountTransactionRequest request) {
        log.info("Deposit request | Account: {}", request.getAccountNumber());
        return ResponseEntity.ok(transactionService.deposit(idempotencyKey, request));
    }

    @Operation(summary = "Withdraw funds from an account")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Withdrawal successful"),
        @ApiResponse(responseCode = "400", description = "Insufficient balance or validation error"),
        @ApiResponse(responseCode = "403", description = "Account FROZEN or INACTIVE"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Concurrent transaction conflict")
    })
    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @RequestHeader("Idempotency-Key")
            @Parameter(description = "Unique UUID to prevent duplicate processing", required = true)
            String idempotencyKey,
            @Valid @RequestBody SingleAccountTransactionRequest request) {
        log.info("Withdrawal request | Account: {}", request.getAccountNumber());
        return ResponseEntity.ok(transactionService.withdraw(idempotencyKey, request));
    }

    @Operation(summary = "Transfer funds between two accounts")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Transfer successful"),
        @ApiResponse(responseCode = "400", description = "Insufficient balance, validation error, or self-transfer"),
        @ApiResponse(responseCode = "403", description = "Account FROZEN or INACTIVE"),
        @ApiResponse(responseCode = "404", description = "Account not found"),
        @ApiResponse(responseCode = "409", description = "Concurrent transaction conflict")
    })
    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @RequestHeader("Idempotency-Key")
            @Parameter(description = "Unique UUID to prevent duplicate processing", required = true)
            String idempotencyKey,
            @Valid @RequestBody TransferRequest request) {
        log.info("Transfer request | From: {} To: {}", request.getSourceAccountNumber(), request.getDestinationAccountNumber());
        return ResponseEntity.ok(transactionService.transfer(idempotencyKey, request));
    }

    @Operation(summary = "Get transaction history for an account",
               description = "Returns all transactions. Optionally filter by date range (yyyy-MM-dd).")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "History returned"),
        @ApiResponse(responseCode = "400", description = "Invalid date range"),
        @ApiResponse(responseCode = "404", description = "Account not found")
    })
    @GetMapping("/history/{accountNumber}")
    public ResponseEntity<List<TransactionHistoryResponse>> getTransactionHistory(
            @PathVariable String accountNumber,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Filter start date (yyyy-MM-dd)") LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            @Parameter(description = "Filter end date (yyyy-MM-dd)") LocalDate endDate) {
        log.info("Transaction history | Account: {} | Range: {} to {}", accountNumber, startDate, endDate);
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber, startDate, endDate));
    }
}
