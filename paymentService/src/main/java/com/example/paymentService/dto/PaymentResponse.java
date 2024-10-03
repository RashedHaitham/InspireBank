package com.example.paymentService.dto;

import lombok.Data;

@Data
public class PaymentResponse {
    private String accountNumber;
    private Double amount;

    public PaymentResponse(String accountNumber, Double amount) {
        this.accountNumber = accountNumber;
        this.amount = amount;
    }
}
