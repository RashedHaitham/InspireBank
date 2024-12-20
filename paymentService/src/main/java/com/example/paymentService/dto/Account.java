package com.example.paymentService.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Account implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    @NotBlank(message = "Account number cannot be blank")
    private String accountNumber;

    @NotNull(message = "Balance cannot be null")
    @DecimalMin(value = "0.0", message = "Balance must be at least 0")
    private Double balance;

    @NotNull(message = "Employee ID cannot be null")
    private Long employeeId;
}
