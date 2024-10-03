package com.example.paymentService.client;

import com.example.paymentService.dto.Account;
import com.example.paymentService.dto.AccountUpdateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "accountService", url = "http://localhost:8084/api/account")
public interface AccountClient {

    @PutMapping("/{id}")
    void updateAccount(@PathVariable("id") String accountNumber, @RequestBody AccountUpdateRequest request);

    @GetMapping("/all")
    List<Account> getAllAccounts();
}
