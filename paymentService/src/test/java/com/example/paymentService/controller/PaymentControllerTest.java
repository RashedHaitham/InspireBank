package com.example.paymentService.controller;

import com.example.paymentService.dto.PaymentResponse;
import com.example.paymentService.model.Payment;
import com.example.paymentService.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createPayment_shouldReturnCreatedPayment() throws Exception {
        // Arrange
        String accountNumber = "acc123";
        Double amount = 500.0;
        Payment payment = new Payment(accountNumber, amount, LocalDateTime.now());
        payment.setId(1L);

        when(paymentService.createPayment(eq(accountNumber), eq(amount))).thenReturn(payment);

        // Act & Assert
        mockMvc.perform(post("/api/payment/{accountNumber}", accountNumber)
                        .param("amount", String.valueOf(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.accountNumber").value("acc123"))
                .andExpect(jsonPath("$.amount").value(500.0));
    }

    @Test
    void createPaymentsForAllEmployees_shouldReturnCreatedPayments() throws Exception {
        // Arrange
        Double amount = 100.0;
        PaymentResponse paymentResponse1 = new PaymentResponse("acc123", 100.0);
        PaymentResponse paymentResponse2 = new PaymentResponse("acc456", 100.0);

        when(paymentService.createPaymentsForAllEmployees(eq(amount))).thenReturn(List.of(paymentResponse1, paymentResponse2));

        // Act & Assert
        mockMvc.perform(post("/api/payment/all")
                        .param("amount", String.valueOf(amount))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].accountNumber").value("acc123"))
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[1].accountNumber").value("acc456"))
                .andExpect(jsonPath("$[1].amount").value(100.0));
    }

    @Test
    void getPaymentsByEmployeeId_shouldReturnPaymentsForAccount() throws Exception {
        // Arrange
        String accountNumber = "acc123";
        Payment payment1 = new Payment(accountNumber, 200.0, LocalDateTime.now());
        payment1.setId(1L);
        Payment payment2 = new Payment(accountNumber, 300.0, LocalDateTime.now());
        payment2.setId(2L);

        when(paymentService.getPaymentsByAccountNumber(accountNumber)).thenReturn(List.of(payment1, payment2));

        // Act & Assert
        mockMvc.perform(get("/api/payment/employee/{accountNumber}", accountNumber)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountNumber").value("acc123"))
                .andExpect(jsonPath("$[0].amount").value(200.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].accountNumber").value("acc123"))
                .andExpect(jsonPath("$[1].amount").value(300.0));
    }

    @Test
    void getAllPayments_shouldReturnAllPayments() throws Exception {
        // Arrange
        Payment payment1 = new Payment("acc123", 200.0, LocalDateTime.now());
        payment1.setId(1L);
        Payment payment2 = new Payment("acc456", 300.0, LocalDateTime.now());
        payment2.setId(2L);

        when(paymentService.getAllPayments()).thenReturn(List.of(payment1, payment2));

        // Act & Assert
        mockMvc.perform(get("/api/payment")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].accountNumber").value("acc123"))
                .andExpect(jsonPath("$[0].amount").value(200.0))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].accountNumber").value("acc456"))
                .andExpect(jsonPath("$[1].amount").value(300.0));
    }
}
