package com.example.paymentService.service;

import com.example.paymentService.client.AccountClient;
import com.example.paymentService.dto.Account;
import com.example.paymentService.dto.PaymentResponse;
import com.example.paymentService.kafka.PaymentProducer;
import com.example.paymentService.model.Payment;
import com.example.paymentService.repository.PaymentRepository;
import org.example.PaymentRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

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
        boolean paymentMade = false;
        try {
            accountClient.updateBalance(accountNumber, amount);
            paymentMade = true;

            PaymentRequest paymentRequest = new PaymentRequest();
            paymentRequest.setAmount(amount);
            paymentRequest.setAccountNumber(accountNumber);
            paymentProducer.sendPayment(paymentRequest);
        } catch (Exception e) {
            if (paymentMade) {
                accountClient.rollbackBalance(accountNumber, amount);
            }
            throw new RuntimeException("Transaction failed, rolling back changes.", e);
        }
        return payment;
    }

    public List<Payment> getPaymentsByAccountNumber(String accountNumber) {
        List<Payment> account=paymentRepository.findByAccountNumber(accountNumber);
        System.out.println(accountNumber);
        if (account.isEmpty()){
            throw new NoSuchElementException("No payment history found for account number: " + accountNumber);
        }
        return account;
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    @Transactional
    public List<PaymentResponse> createPaymentsForAllEmployees(Double amount) {
        List<PaymentResponse> paymentResponses = new ArrayList<>();

        int pageNumber = 0;
        int pageSize = 20;  // page size based on performance and expected load
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Page<Account> accountPage;
        do {
            accountPage = accountClient.getAllAccounts(pageable);

            for (Account account : accountPage.getContent()) {
                Payment payment = new Payment(account.getAccountNumber(), amount, LocalDateTime.now());

                accountClient.updateBalance(account.getAccountNumber(), amount);

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

            pageNumber++;
            pageable = PageRequest.of(pageNumber, pageSize);

        } while (accountPage.hasNext());

        return paymentResponses;
    }


}