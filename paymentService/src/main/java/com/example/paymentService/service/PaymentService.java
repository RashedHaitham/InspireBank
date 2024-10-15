package com.example.paymentService.service;

import com.example.paymentService.client.AccountClient;
import com.example.paymentService.dto.Account;
import com.example.paymentService.dto.AccountUpdateRequest;
import com.example.paymentService.dto.PaymentResponse;
import com.example.paymentService.kafka.PaymentProducer;
import com.example.paymentService.model.Payment;
import com.example.paymentService.repository.PaymentRepository;
import org.example.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Validated
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AccountClient accountClient;
    private final PaymentProducer paymentProducer;

    @Autowired
    public PaymentService(PaymentRepository paymentRepository, AccountClient accountClient, PaymentProducer paymentProducer) {
        this.paymentRepository = paymentRepository;
        this.accountClient = accountClient;
        this.paymentProducer = paymentProducer;
    }

    @Transactional
    public Payment createPayment(String accountNumber, Double amount) {
        Payment payment = new Payment(accountNumber,amount, LocalDateTime.now());
        payment = paymentRepository.save(payment);

        AccountUpdateRequest updateRequest = new AccountUpdateRequest();
        updateRequest.setAccountNumber(accountNumber);
        updateRequest.setInitialBalance(amount);

        updateRequest.setEmployeeId(accountClient.getAccountByNumber(accountNumber).getEmployeeId());

        accountClient.updateAccount(accountNumber, updateRequest);

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setAmount(amount);
        paymentRequest.setAccountNumber(accountNumber);
        paymentProducer.sendPayment(paymentRequest);

        return payment;
    }

    public List<Payment> getPaymentsByAccountNumber(String accountNumber) {
        return paymentRepository.findByAccountNumber(accountNumber);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional
    public List<PaymentResponse> createPaymentsForAllEmployees(Double amount) {

        List<PaymentResponse> paymentResponses = new ArrayList<>();
        List<Account> accounts = accountClient.getAllAccounts();
        AccountUpdateRequest request = new AccountUpdateRequest();

        for (Account account : accounts) {
            Payment payment = new Payment(account.getAccountNumber(), amount, LocalDateTime.now());
            request.setInitialBalance(amount);
            accountClient.updateAccount(account.getAccountNumber(), request);

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(amount);
            paymentRequest.setAccountNumber(account.getAccountNumber());
            paymentProducer.sendPayment(paymentRequest);

            paymentRepository.save(payment);

            PaymentResponse paymentResponse = new PaymentResponse(
                    payment.getAccountNumber(),
                    payment.getAmount()
            );

            paymentResponses.add(paymentResponse);
        }

        return paymentResponses;
    }

}