package com.example.notification;

import org.springframework.stereotype.Component;

@Component("sms")
public class SmsNotification implements NotificationService {

    @Override
    public String sendNotification(String message) {
        return "SMS sent: " + message;
    }
}