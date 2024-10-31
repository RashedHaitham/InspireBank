package com.example.accountService.service;

import com.example.accountService.client.EmployeeClient;
import com.example.accountService.dto.AccountCreationRequest;
import com.example.accountService.dto.EmployeeResponse;
import com.example.accountService.model.Account;
import com.example.accountService.repository.AccountRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private EmployeeClient employeeClient;

    @InjectMocks
    private AccountService accountService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createAccount_shouldCreateAccountSuccessfully() {
        AccountCreationRequest request = new AccountCreationRequest(123L, "acc123", 1000.0);
        EmployeeResponse employeeResponse = new EmployeeResponse(123L, "Rahed alqatarneh", "rashed@test.com", "Developer");

        when(employeeClient.getEmployeeById(123L)).thenReturn(employeeResponse);
        when(accountRepository.findByEmployeeId(123L)).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumber("acc123")).thenReturn(Optional.empty());
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        Account createdAccount = accountService.createAccount(request);

        assertNotNull(createdAccount);
        assertEquals("acc123", createdAccount.getAccountNumber());
        assertEquals(1000.0, createdAccount.getBalance());
        assertEquals(123L, createdAccount.getEmployeeId());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void createAccount_shouldThrowExceptionIfEmployeeNotFound() {
        AccountCreationRequest request = new AccountCreationRequest(123L, "acc123", 1000.0);

        when(employeeClient.getEmployeeById(123L)).thenReturn(null);

        assertThrows(ResourceNotFoundException.class, () -> accountService.createAccount(request));
    }

    @Test
    void updateAccount_shouldUpdateAccountSuccessfully() {
        String accountId = "acc123";
        Account existingAccount = Account.builder().accountNumber("acc123").balance(500.0).employeeId(123L).build();
        AccountCreationRequest request = new AccountCreationRequest(123L, "acc456", 200.0);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        when(employeeClient.getEmployeeById(123L)).thenReturn(new EmployeeResponse(123L, "rashed alqatarneh", "rashed@test.com", "Developer"));
        when(accountRepository.save(any(Account.class))).thenAnswer(i -> i.getArguments()[0]);

        Account updatedAccount = accountService.updateAccount(accountId, request);

        assertNotNull(updatedAccount);
        assertEquals("acc456", updatedAccount.getAccountNumber());
        assertEquals(700.0, updatedAccount.getBalance());
        assertEquals(123L, updatedAccount.getEmployeeId());
        verify(accountRepository, times(1)).save(existingAccount);
    }

    @Test
    void updateAccount_shouldThrowExceptionIfAccountNotFound() {
        String accountId = "acc123";
        AccountCreationRequest request = new AccountCreationRequest(123L, "acc456", 200.0);

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.updateAccount(accountId, request));
    }

    @Test
    void deleteAccount_shouldDeleteAccountSuccessfully() {
        String accountId = "acc123";
        Account existingAccount = Account.builder().accountNumber("acc123").balance(500.0).employeeId(123L).build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));
        doNothing().when(accountRepository).deleteById(accountId);

        accountService.deleteAccount(accountId);

        verify(accountRepository, times(1)).deleteById(accountId);
    }

    @Test
    void deleteAccount_shouldThrowExceptionIfAccountNotFound() {
        String accountId = "acc123";

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.deleteAccount(accountId));
    }

    @Test
    void getAccountById_shouldReturnAccountSuccessfully() {
        String accountId = "acc123";
        Account existingAccount = Account.builder().accountNumber("acc123").balance(500.0).employeeId(123L).build();

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));

        Account account = accountService.getAccountById(accountId);

        assertNotNull(account);
        assertEquals("acc123", account.getAccountNumber());
        verify(accountRepository, times(1)).findById(accountId);
    }

    @Test
    void getAccountById_shouldThrowExceptionIfAccountNotFound() {
        String accountId = "acc123";

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getAccountById(accountId));
    }

    @Test
    void getAllAccounts_shouldReturnPaginatedAccountsSuccessfully() {
        // Arrange
        Account account1 = Account.builder()
                .accountNumber("acc123")
                .balance(500.0)
                .employeeId(123L)
                .build();

        Account account2 = Account.builder()
                .accountNumber("acc456")
                .balance(1000.0)
                .employeeId(456L)
                .build();

        List<Account> accounts = List.of(account1, account2);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("balance").ascending());
        Page<Account> accountsPage = new PageImpl<>(accounts, pageable, accounts.size());

        when(accountRepository.findAll(pageable)).thenReturn(accountsPage);

        // Act
        Page<Account> result = accountService.getAllAccounts(pageable);

        // Assert
        assertNotNull(result, "The result should not be null");
        assertEquals(2, result.getContent().size(), "There should be 2 accounts in the result");
        assertEquals("acc123", result.getContent().get(0).getAccountNumber(), "First account number should be acc123");
        assertEquals(500.0, result.getContent().get(0).getBalance(), "First account balance should be 500.0");
        assertEquals(123L, result.getContent().get(0).getEmployeeId(), "First account employeeId should be 123L");
        assertEquals("acc456", result.getContent().get(1).getAccountNumber(), "Second account number should be acc456");
        assertEquals(1000.0, result.getContent().get(1).getBalance(), "Second account balance should be 1000.0");
        assertEquals(456L, result.getContent().get(1).getEmployeeId(), "Second account employeeId should be 456L");

        // Verify that the repository's findAll method was called once with the correct pageable
        verify(accountRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllAccounts_shouldThrowExceptionIfNoAccountsFound() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 2, Sort.by("balance").ascending());
        Page<Account> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(accountRepository.findAll(pageable)).thenReturn(emptyPage);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            accountService.getAllAccounts(pageable);
        }, "Expected getAllAccounts to throw ResourceNotFoundException when no accounts are found");

        assertEquals("No accounts found", exception.getMessage(), "Exception message should match the expected value");

        // Verify that the repository's findAll method was called once with the correct pageable
        verify(accountRepository, times(1)).findAll(pageable);
    }
}
