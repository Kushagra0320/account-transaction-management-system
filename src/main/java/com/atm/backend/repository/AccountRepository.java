package com.atm.backend.repository;

import com.atm.backend.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * JPA Repository for Account entity.
 * Spring Data JPA generates the implementation at runtime.
 */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    /**
     * Finds an account by its external-facing account number (not the internal PK).
     * Used by all transaction operations and the AccountController.
     */
    Optional<Account> findByAccountNumber(String accountNumber);
}
