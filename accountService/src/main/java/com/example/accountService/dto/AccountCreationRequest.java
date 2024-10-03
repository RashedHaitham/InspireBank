package com.example.accountService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationRequest {
    private Long employeeId;
    private String accountNumber;
    private Double initialBalance;
}
