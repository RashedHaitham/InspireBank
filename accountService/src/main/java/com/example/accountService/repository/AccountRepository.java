package com.example.accountService.repository;

import com.example.accountService.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByEmployeeId(Long employeeId);

    Optional<Account> findByAccountNumber(String accountNumber);
}
