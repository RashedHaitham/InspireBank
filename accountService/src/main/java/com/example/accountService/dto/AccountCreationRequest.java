package com.example.accountService.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.NotNull;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountCreationRequest {
    @NotNull(message = "Employee ID cannot be null")
    private Long employeeId;

    @NotBlank(message = "Account number cannot be blank")
    @Pattern(regexp = "^[A-Za-z0-9]{10,20}$", message = "Account number must be alphanumeric and between 10 to 20 characters")
    private String accountNumber;

    @NotNull(message = "Initial balance cannot be null")
    @DecimalMin(value = "0.0", inclusive = false, message = "Initial balance must be greater than 0")
    private Double initialBalance;
}
