package com.example.paymentService.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountUpdateRequest {

    @NotNull(message = "Employee ID cannot be null")
    private Long employeeId;

    @NotBlank(message = "Account number cannot be blank")
    private String accountNumber;

    @NotNull(message = "Initial balance cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial balance must be greater than 0")
    private Double initialBalance;
}
