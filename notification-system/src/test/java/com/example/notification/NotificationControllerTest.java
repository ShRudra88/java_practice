package com.example.notification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void sendsEmailNotification() throws Exception {
        mockMvc.perform(get("/api/notifications/email")
                        .param("message", "Hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("Email sent: Hello"));
    }

    @Test
    void sendsSmsNotification() throws Exception {
        mockMvc.perform(get("/api/notifications/sms")
                        .param("message", "Hello"))
                .andExpect(status().isOk())
                .andExpect(content().string("SMS sent: Hello"));
    }

    @Test
    void requiresMessageParameter() throws Exception {
        mockMvc.perform(get("/api/notifications/email"))
                .andExpect(status().isBadRequest());
    }
}
