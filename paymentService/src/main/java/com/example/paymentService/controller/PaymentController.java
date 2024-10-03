package com.example.paymentService.controller;

import com.example.paymentService.dto.PaymentResponse;
import com.example.paymentService.model.Payment;
import com.example.paymentService.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @Autowired
    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/{accountNumber}")
    public ResponseEntity<Payment> createPayment(@PathVariable String accountNumber,
                                                 @RequestParam Double amount) {
        Payment payment = paymentService.createPayment(accountNumber, amount);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @PostMapping("/all")
    public ResponseEntity<List<PaymentResponse>> createPaymentsForAllEmployees(@RequestParam Double amount) {
        List<PaymentResponse> payments = paymentService.createPaymentsForAllEmployees(amount);
        return ResponseEntity.status(HttpStatus.CREATED).body(payments);
    }

    @GetMapping("/employee/{accountNumber}")
    public ResponseEntity<List<Payment>> getPaymentsByEmployeeId(@PathVariable String accountNumber) {
        List<Payment> payments = paymentService.getPaymentsByAccountNumber(accountNumber);
        return ResponseEntity.ok(payments);
    }

    @GetMapping
    public ResponseEntity<List<Payment>> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
}