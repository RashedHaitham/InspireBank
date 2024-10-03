package com.example.paymentService.dto;

import lombok.Data;

@Data
public class AccountUpdateRequest {
    private Long employeeId;
    private String accountNumber;
    private Double initialBalance;
}
