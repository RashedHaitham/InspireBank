package com.example.paymentService.client;

import com.example.paymentService.dto.Account;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@FeignClient(name = "accountService", path = "/api/account")
public interface AccountClient {

    @PostMapping("/{id}")
    void updateBalance(@PathVariable("id") String accountNumber, @RequestBody Double amount);

    @PostMapping("/{id}/rollback")
    void rollbackBalance(@PathVariable("id") String accountNumber, @RequestBody Double amount);

    @GetMapping("/all")
    Page<Account> getAllAccounts(Pageable pageable);
}
