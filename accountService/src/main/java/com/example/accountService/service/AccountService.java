package com.example.accountService.service;

import com.example.accountService.client.EmployeeClient;
import com.example.accountService.dto.AccountCreationRequest;
import com.example.accountService.dto.EmployeeResponse;
import com.example.accountService.model.Account;
import com.example.accountService.repository.AccountRepository;
import org.springframework.cache.annotation.Cacheable;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Service
@Validated
public class AccountService {

    private final AccountRepository accountRepository;
    private final EmployeeClient employeeClient;

    @Autowired
    public AccountService(AccountRepository accountRepository, EmployeeClient employeeClient) {
        this.accountRepository = accountRepository;
        this.employeeClient = employeeClient;
    }

    public Account createAccount(AccountCreationRequest request) {

        EmployeeResponse employee = employeeClient.getEmployeeById(request.getEmployeeId());

        Optional.ofNullable(employee)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        accountRepository.findByEmployeeId(request.getEmployeeId())
                .ifPresent(account -> {
                    throw new IllegalArgumentException("Employee already has an account");
                });

        accountRepository.findByAccountNumber(request.getAccountNumber())
                .ifPresent(account -> {
                    throw new IllegalArgumentException("Account number already exists");
                });

        Account account = Account.builder()
                .accountNumber(request.getAccountNumber())
                .balance(request.getInitialBalance())
                .employeeId(request.getEmployeeId())
                .build();

        return accountRepository.save(account);
    }


    public Account updateAccount(String id, AccountCreationRequest request) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));

        Optional.ofNullable(employeeClient.getEmployeeById(account.getEmployeeId()))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Optional.ofNullable(request.getAccountNumber())
                .ifPresent(account::setAccountNumber);

        Optional.ofNullable(request.getInitialBalance())
                .ifPresent(initialBalance -> account.setBalance(account.getBalance() + initialBalance));

        Optional.ofNullable(request.getEmployeeId())
                .ifPresent(account::setEmployeeId);

        return accountRepository.save(account);
    }

    @CacheEvict(key = "#id",value = "Account")
    public void deleteAccount(String id) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if (optionalAccount.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with ID: " + id);
        }

        accountRepository.deleteById(id);
    }

    @Cacheable(key = "#id",value = "Account")
    public Account getAccountById(String id) {
        System.out.println("no cache, getting account number "+id+" from DB...");
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if (optionalAccount.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with ID: " + id);
        }
        return optionalAccount.get();
    }

    @Cacheable(value = "Account")
    public List<Account> getAllAccounts() {
        System.out.println("no cache, getting all accounts from DB...");
        List<Account> optionalAccounts = accountRepository.findAll();
        if (optionalAccounts.isEmpty()) {
            throw new ResourceNotFoundException("No accounts found");
        }
        return optionalAccounts;
    }
}
