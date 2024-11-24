package com.example.accountService.controller;

import com.example.accountService.dto.AccountCreationRequest;
import com.example.accountService.model.Account;
import com.example.accountService.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/account")
public class AccountController {

    private final AccountService accountService;

    @Autowired
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(@Valid @RequestBody AccountCreationRequest request) {
        Account account = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(account);
    }

    @PutMapping("/{accountNumber}")
    public ResponseEntity<Account> updateAccount(@PathVariable String accountNumber,@Valid @RequestBody AccountCreationRequest request) {
        Account updatedAccount = accountService.updateAccount(accountNumber, request);
        return ResponseEntity.ok(updatedAccount);
    }

    @PostMapping("/{accountNumber}")
    public ResponseEntity<Account> updateBalance(@PathVariable String accountNumber,@RequestBody Double amount) {
        Account updatedAccount = accountService.updateBalance(accountNumber, amount);
        System.out.println(updatedAccount);
        return ResponseEntity.ok(updatedAccount);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAccount(@PathVariable String id) {
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<Account> getAccountByNumber(@PathVariable String accountNumber) {
        Account account = accountService.getAccountById(accountNumber);
        return ResponseEntity.ok(account);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<Account>> getAllAccounts(Pageable pageable) {
        Page<Account> accounts = accountService.getAllAccounts(pageable);
        return ResponseEntity.ok(accounts);
    }

    @PostMapping("/{id}/rollback")
    public ResponseEntity<Void> rollbackBalance(@PathVariable("id") String accountNumber, @RequestBody Double amount) {
        accountService.rollbackBalance(accountNumber, amount);
        return ResponseEntity.ok().build();
    }

}
