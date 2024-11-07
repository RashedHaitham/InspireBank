package com.example.paymentService.controller;

import com.example.paymentService.dto.PaymentResponse;
import com.example.paymentService.model.Payment;
import com.example.paymentService.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class PaymentGraphQLController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentGraphQLController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @MutationMapping
    public Payment createPayment(@Argument String accountNumber, @Argument Double amount) {
        return paymentService.createPayment(accountNumber, amount);
    }

    @MutationMapping
    public List<PaymentResponse> createPaymentsForAllEmployees(@Argument Double amount) {
        return paymentService.createPaymentsForAllEmployees(amount);
    }

    @QueryMapping
    public List<Payment> getPaymentsByAccountNumber(@Argument String accountNumber) {
        return paymentService.getPaymentsByAccountNumber(accountNumber);
    }

    @QueryMapping
    public List<Payment> getAllPayments() {
        return paymentService.getAllPayments();
    }
}
