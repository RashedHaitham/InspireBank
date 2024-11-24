package com.example.apiGateway.model;

import lombok.Data;

@Data
public class UserDTO {
    private String username;
    private String fullName;
    private String lastName;
    private String email;
    private String role;
}
