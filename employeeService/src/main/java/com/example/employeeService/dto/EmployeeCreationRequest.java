package com.example.employeeService.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeCreationRequest {
    @NotBlank(message = "Employee name cannot be null or blank")
    @Size(min = 2, max = 50, message = "Employee name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Email cannot be null or blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Position cannot be null or blank")
    private String position;
}
