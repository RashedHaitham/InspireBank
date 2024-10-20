package com.example.accountService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account implements Serializable {

    @Serial
    private static final long serialVersionUID = 2L;

    @Id
    private String accountNumber;

    private Double balance;
    private Long employeeId;
}
