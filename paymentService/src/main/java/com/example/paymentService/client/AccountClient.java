package com.example.paymentService.client;

import com.example.paymentService.dto.Account;
import com.example.paymentService.dto.AccountUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "accountService", path = "/api/account")
public interface AccountClient {

    @PostMapping("/{id}")
    void updateBalance(@PathVariable("id") String accountNumber, @RequestBody Double amount);

    @GetMapping("/all")
    Page<Account> getAllAccounts(Pageable pageable);
}
