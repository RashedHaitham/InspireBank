package com.example.accountService.controller;

import com.example.accountService.dto.AccountCreationRequest;
import com.example.accountService.model.Account;
import com.example.accountService.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
public class AccountGraphQLController {

    private final AccountService accountService;

    @Autowired
    public AccountGraphQLController(AccountService accountService) {
        this.accountService = accountService;
    }

    @MutationMapping
    public Account createAccount(@Argument @Valid AccountCreationRequest request) {
        return accountService.createAccount(request);
    }

    @MutationMapping
    public Account updateAccount(@Argument String accountNumber, @Argument @Valid AccountCreationRequest request) {
        return accountService.updateAccount(accountNumber, request);
    }

    @MutationMapping
    public Account updateBalance(@Argument String accountNumber, @Argument Double amount) {
        return accountService.updateBalance(accountNumber, amount);
    }

    @MutationMapping
    public String deleteAccount(@Argument String id) {
        accountService.deleteAccount(id);
        return "Account deleted";
    }

    @QueryMapping
    public Account getAccountByNumber(@Argument String accountNumber) {
        return accountService.getAccountById(accountNumber);
    }

    @QueryMapping
    public Page<Account> getAllAccounts(@Argument int page, @Argument int size) {
        Pageable pageable = PageRequest.of(page, size);
        return accountService.getAllAccounts(pageable);
    }

    @MutationMapping
    public Boolean rollbackBalance(@Argument String accountNumber, @Argument Double amount) {
        accountService.rollbackBalance(accountNumber, amount);
        return true;
    }
}
