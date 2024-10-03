package com.example.paymentService.dto;

import lombok.Data;

@Data
public class Account {
    private String accountNumber;
    private Double balance;
    private Long employeeId;
}
