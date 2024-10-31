package com.example.accountService.service;

import com.example.accountService.client.EmployeeClient;
import com.example.accountService.dto.AccountCreationRequest;
import com.example.accountService.model.Account;
import com.example.accountService.repository.AccountRepository;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;

@Service
@Validated
public class AccountService {

    private final AccountRepository accountRepository;
    private final EmployeeClient employeeClient;
    private static final String ACCOUNT_CACHE = "Account";

    @Autowired
    public AccountService(AccountRepository accountRepository, EmployeeClient employeeClient) {
        this.accountRepository = accountRepository;
        this.employeeClient = employeeClient;
    }

    @CachePut(value = ACCOUNT_CACHE, key = "#request.accountNumber")
    public Account createAccount(AccountCreationRequest request) {

        employeeClient.getEmployeeById(request.getEmployeeId());

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

    @CachePut(value = ACCOUNT_CACHE, key = "#accountNumber")
    public Account updateAccount(String accountNumber, AccountCreationRequest request) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + accountNumber));

        Optional.ofNullable(employeeClient.getEmployeeById(account.getEmployeeId()))
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found"));

        Optional.ofNullable(request.getAccountNumber())
                .ifPresent(account::setAccountNumber);

        Optional.ofNullable(request.getInitialBalance())
                .ifPresent(account::setBalance);

        Optional.ofNullable(request.getEmployeeId())
                .ifPresent(account::setEmployeeId);

        return accountRepository.save(account);
    }

    @CachePut(value = ACCOUNT_CACHE, key = "#accountNumber")
    public Account updateBalance(String accountNumber, Double amount) {
        Account account = accountRepository.findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with number: " + accountNumber));

        Double newBalance = account.getBalance() + amount;
        account.setBalance(newBalance);

        return accountRepository.save(account);
    }

    @CacheEvict(value = ACCOUNT_CACHE, key = "#id")
    @Transactional
    public void deleteAccount(String id) {
        Optional<Account> optionalAccount = accountRepository.findById(id);
        if (optionalAccount.isEmpty()) {
            throw new ResourceNotFoundException("Account not found with number: " + id);
        }

        accountRepository.deleteById(id);
    }

    @Cacheable(value = ACCOUNT_CACHE, key = "#id")
    @Transactional(readOnly = true)
    public Account getAccountById(String id) {
        System.out.println("No cache, getting account number " + id + " from DB...");
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found with ID: " + id));
    }

    @Cacheable(value = ACCOUNT_CACHE, key = "'page-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort")
    @Transactional(readOnly = true)
    public Page<Account> getAllAccounts(Pageable pageable) {
        System.out.println("No cache, fetching accounts from the database...");
        Page<Account> accountsPage = accountRepository.findAll(pageable);
        if (accountsPage.isEmpty()) {
            throw new ResourceNotFoundException("No accounts found");
        }
        return accountsPage;
    }
}
