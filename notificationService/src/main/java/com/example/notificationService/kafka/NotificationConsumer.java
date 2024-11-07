package com.example.notificationService.kafka;

import com.example.notificationService.model.Notification;
import org.example.PaymentRequest;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class NotificationConsumer {

    private static final Logger logger = Logger.getLogger(NotificationConsumer.class.getName());

    private void sendNotificationToUser(Notification message) {
        logger.info("Sending notification to user: " + message);
    }

    /*@KafkaListener(topics = "payment-topic", groupId = "paymentService")
    public void consumeNotification(PaymentRequest paymentRequest) {
        logger.info("Received payment notification: " + paymentRequest);

        Notification notificationMessage = createNotificationMessage(paymentRequest.getAccountNumber(),
                paymentRequest.getAmount());

        sendNotificationToUser(notificationMessage);
    }*/

    @RabbitListener(queues = "${rabbitmq.queue.name}")
    public void consumeNotification(PaymentRequest paymentRequest) {
        System.out.println("Received message: " + paymentRequest);

        Notification notificationMessage = createNotificationMessage(paymentRequest.getAccountNumber(),
                paymentRequest.getAmount());

        sendNotificationToUser(notificationMessage);
    }

    private Notification createNotificationMessage(String accountNumber, double amount) {
        Notification notification = new Notification();
        notification.setMessage("account number: " + accountNumber + "has been paid successfully with amount: " + amount);
        notification.setRecipient(accountNumber);
        return notification;
    }
}
