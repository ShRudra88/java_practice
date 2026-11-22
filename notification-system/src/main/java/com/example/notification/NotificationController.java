package com.example.notification;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService email;
    private final NotificationService sms;

    public NotificationController(
            @Qualifier("email")
            NotificationService email,
            @Qualifier("sms")
            NotificationService sms) {

        this.email = email;
        this.sms = sms;
    }

    @GetMapping("/email")
    public String sendEmail(@RequestParam String message) {
        return email.sendNotification(message);
    }

    @GetMapping("/sms")
    public String sendSms(@RequestParam String message) {
        return sms.sendNotification(message);
    }
}