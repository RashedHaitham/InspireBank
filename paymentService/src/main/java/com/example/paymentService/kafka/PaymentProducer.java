package com.example.paymentService.kafka;

import org.example.PaymentRequest;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class PaymentProducer {

    private static final Logger logger = Logger.getLogger(PaymentProducer.class.getName());
    private static final String QUEUE_NAME  = "payment-queue";

    private final RabbitTemplate rabbitTemplate;

    @Autowired
    public PaymentProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendPayment(PaymentRequest paymentRequest) {
        logger.info("Sending payment request: " + paymentRequest);

        rabbitTemplate.convertAndSend(QUEUE_NAME, paymentRequest);
    }

   /* private final KafkaTemplate<String, PaymentRequest> kafkaTemplate;

    @Autowired
    public PaymentProducer(KafkaTemplate<String, PaymentRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendPayment(PaymentRequest paymentRequest) {
        logger.info("Sending payment request: " + paymentRequest);
        Message<PaymentRequest> message = MessageBuilder.withPayload(paymentRequest)
                .setHeader(KafkaHeaders.TOPIC, TOPIC)
                .setHeader(KafkaHeaders.KEY, paymentRequest.getAccountNumber())
                .build();
        kafkaTemplate.send(message);
    }*/


}
