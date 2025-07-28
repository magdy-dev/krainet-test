package com.krainet.notificationservice.config;


import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;


import java.util.Properties;

/**
 * Test configuration for the notification service tests.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    public JavaMailSender testMailSender() {
        // Using a dummy mail sender for testing
        return new JavaMailSenderImpl();
    }

    @Bean
    public String emailFrom() {
        return "test@krainet.com";
    }
}
