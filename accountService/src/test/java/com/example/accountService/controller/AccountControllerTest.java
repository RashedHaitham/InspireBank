package com.example.accountService.controller;

import com.example.accountService.dto.AccountCreationRequest;
import com.example.accountService.model.Account;
import com.example.accountService.service.AccountService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_shouldReturnCreatedAccount() throws Exception {
        AccountCreationRequest request = new AccountCreationRequest(123L, "acc1234567", 1000.0);
        Account account = Account.builder().accountNumber("acc1234567").balance(1000.0).employeeId(123L).build();

        when(accountService.createAccount(any(AccountCreationRequest.class))).thenReturn(account);

        mockMvc.perform(post("/api/account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("acc1234567"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.employeeId").value(123));
    }

    @Test
    void updateAccount_shouldReturnUpdatedAccount() throws Exception {
        String accountId = "acc1234567";
        AccountCreationRequest request = new AccountCreationRequest(123L, "acc4567890", 500.0);
        Account updatedAccount = Account.builder().accountNumber("acc4567890").balance(1500.0).employeeId(123L).build();

        when(accountService.updateAccount(eq(accountId), any(AccountCreationRequest.class))).thenReturn(updatedAccount);

        mockMvc.perform(put("/api/account/{id}", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("acc4567890"))
                .andExpect(jsonPath("$.balance").value(1500.0))
                .andExpect(jsonPath("$.employeeId").value(123));
    }

    @Test
    void deleteAccount_shouldReturnNoContent() throws Exception {
        String accountId = "acc1234567";
        doNothing().when(accountService).deleteAccount(accountId);

        mockMvc.perform(delete("/api/account/{id}", accountId))
                .andExpect(status().isNoContent());
    }

    @Test
    void getAccountById_shouldReturnAccount() throws Exception {
        // Arrange
        String accountId = "acc123";
        Account account = Account.builder().accountNumber("acc123").balance(1000.0).employeeId(123L).build();

        when(accountService.getAccountById(accountId)).thenReturn(account);

        mockMvc.perform(get("/api/account/{id}", accountId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accountNumber").value("acc123"))
                .andExpect(jsonPath("$.balance").value(1000.0))
                .andExpect(jsonPath("$.employeeId").value(123));
    }

    @Test
    void getAllAccounts_shouldReturnPaginatedAccounts() throws Exception {
        // Arrange
        Account account1 = Account.builder()
                .accountNumber("acc123")
                .balance(500.0)
                .employeeId(123L)
                .build();

        Account account2 = Account.builder()
                .accountNumber("acc456")
                .balance(1500.0)
                .employeeId(456L)
                .build();

        List<Account> accounts = List.of(account1, account2);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("balance").ascending());
        Page<Account> accountsPage = new PageImpl<>(accounts, pageable, 2);

        when(accountService.getAllAccounts(any(Pageable.class))).thenReturn(accountsPage);

        // Act & Assert
        mockMvc.perform(get("/api/account/all")
                        .param("page", "0")
                        .param("size", "2")
                        .param("sort", "balance,asc")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                // Verify pagination metadata
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].accountNumber").value("acc123"))
                .andExpect(jsonPath("$.content[0].balance").value(500.0))
                .andExpect(jsonPath("$.content[0].employeeId").value(123))
                .andExpect(jsonPath("$.content[1].accountNumber").value("acc456"))
                .andExpect(jsonPath("$.content[1].balance").value(1500.0))
                .andExpect(jsonPath("$.content[1].employeeId").value(456))
                // Verify pagination details
                .andExpect(jsonPath("$.pageable.pageNumber").value(0))
                .andExpect(jsonPath("$.pageable.pageSize").value(2))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.last").value(true))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.numberOfElements").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.empty").value(false));
    }
}
