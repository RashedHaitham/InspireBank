package com.example.paymentService.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String accountNumber;
    private Double amount;
    private LocalDateTime timestamp;

    public Payment() {}

    public Payment(String accountNumber, Double amount, LocalDateTime now) {
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.timestamp = now;
    }
}