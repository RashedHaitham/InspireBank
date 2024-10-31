package com.example.paymentService.service;

import com.example.paymentService.client.AccountClient;
import com.example.paymentService.dto.Account;
import com.example.paymentService.dto.AccountUpdateRequest;
import com.example.paymentService.dto.PaymentResponse;
import com.example.paymentService.kafka.PaymentProducer;
import com.example.paymentService.model.Payment;
import com.example.paymentService.repository.PaymentRepository;
import org.example.PaymentRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AccountClient accountClient;

    @Mock
    private PaymentProducer paymentProducer;

    @InjectMocks
    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPayment_shouldReturnCreatedPayment() {
        // Arrange
        String accountNumber = "acc123";
        Double amount = 500.0;
        Payment payment = new Payment(accountNumber, amount, LocalDateTime.now());
        payment.setId(1L);

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        doNothing().when(accountClient).updateBalance(eq(accountNumber),amount);
        doNothing().when(paymentProducer).sendPayment(any(PaymentRequest.class));

        // Act
        Payment createdPayment = paymentService.createPayment(accountNumber, amount);

        // Assert
        assertNotNull(createdPayment);
        assertEquals(1L, createdPayment.getId());
        assertEquals(accountNumber, createdPayment.getAccountNumber());
        assertEquals(amount, createdPayment.getAmount());
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(accountClient, times(1)).updateBalance(eq(accountNumber),amount);
        verify(paymentProducer, times(1)).sendPayment(any(PaymentRequest.class));
    }

    @Test
    void getPaymentsByAccountNumber_shouldReturnPayments() {
        // Arrange
        String accountNumber = "acc123";
        Payment payment1 = new Payment(accountNumber, 200.0, LocalDateTime.now());
        payment1.setId(1L);
        Payment payment2 = new Payment(accountNumber, 300.0, LocalDateTime.now());
        payment2.setId(2L);

        when(paymentRepository.findByAccountNumber(accountNumber)).thenReturn(List.of(payment1, payment2));

        // Act
        List<Payment> payments = paymentService.getPaymentsByAccountNumber(accountNumber);

        // Assert
        assertNotNull(payments);
        assertEquals(2, payments.size());
        assertEquals(1L, payments.get(0).getId());
        assertEquals(2L, payments.get(1).getId());
        verify(paymentRepository, times(1)).findByAccountNumber(accountNumber);
    }

    @Test
    void getAllPayments_shouldReturnAllPayments() {
        // Arrange
        Payment payment1 = new Payment("acc123", 200.0, LocalDateTime.now());
        payment1.setId(1L);
        Payment payment2 = new Payment("acc456", 300.0, LocalDateTime.now());
        payment2.setId(2L);

        when(paymentRepository.findAll()).thenReturn(List.of(payment1, payment2));

        // Act
        List<Payment> payments = paymentService.getAllPayments();

        // Assert
        assertNotNull(payments);
        assertEquals(2, payments.size());
        assertEquals(1L, payments.get(0).getId());
        assertEquals(2L, payments.get(1).getId());
        verify(paymentRepository, times(1)).findAll();
    }

    @Test
    void createPaymentsForAllEmployees_shouldReturnPaymentResponses() {
        // Arrange
        Double amount = 100.0;
        Account account1 = new Account();
        account1.setAccountNumber("acc123");
        account1.setBalance(1000.0);
        account1.setEmployeeId(1L);

        Account account2 = new Account();
        account2.setAccountNumber("acc456");
        account2.setBalance(1500.0);
        account2.setEmployeeId(2L);

        List<Account> accounts = List.of(account1, account2);

        // Create a pageable list (single page) to simulate paginated response
        Pageable pageable = PageRequest.of(0, 2);
        Page<Account> accountPage = new PageImpl<>(accounts, pageable, accounts.size());

        // Mock the paginated response
        when(accountClient.getAllAccounts(any(Pageable.class))).thenReturn(accountPage);

        // Mock save behavior for payment repository
        when(paymentRepository.save(any(Payment.class))).thenAnswer(i -> {
            Payment savedPayment = i.getArgument(0);
            savedPayment.setId(savedPayment.getAccountNumber().equals("acc123") ? 1L : 2L);
            return savedPayment;
        });

        // Mock behavior for updating account and sending payment
        doNothing().when(accountClient).updateBalance(anyString(),amount );
        doNothing().when(paymentProducer).sendPayment(any(PaymentRequest.class));

        // Act
        List<PaymentResponse> paymentResponses = paymentService.createPaymentsForAllEmployees(amount);

        // Assert
        assertNotNull(paymentResponses);
        assertEquals(2, paymentResponses.size());
        assertEquals("acc123", paymentResponses.get(0).getAccountNumber());
        assertEquals(amount, paymentResponses.get(0).getAmount());
        assertEquals("acc456", paymentResponses.get(1).getAccountNumber());

        // Verify each method call was made twice, once per account
        verify(accountClient, times(2)).updateBalance(anyString(),amount);
        verify(paymentProducer, times(2)).sendPayment(any(PaymentRequest.class));
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }
}
