package com.example.notification;

import org.springframework.stereotype.Component;

@Component("email")
public class EmailNotification implements NotificationService {

    @Override
    public String sendNotification(String message) {
        return "Email sent: " + message;
    }
}