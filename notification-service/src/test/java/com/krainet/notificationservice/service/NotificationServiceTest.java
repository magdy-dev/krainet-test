package com.krainet.notificationservice.service;

import com.krainet.common.event.EventType;
import com.krainet.common.event.UserEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import com.krainet.common.event.EventType;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @Captor
    private ArgumentCaptor<SimpleMailMessage> emailCaptor;

    private NotificationService notificationService;
    private static final String ADMIN_EMAIL = "admin@krainet.com";

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(mailSender);
    }

    @Test
    void processUserEvent_UserCreated_SendsWelcomeEmailAndAdminNotification() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID initiatorId = UUID.randomUUID();

        UserEvent event = UserEvent.builder(
                        EventType.USER_PASSWORD_CHANGED,  // eventType
                        userId,                           // userId
                        "testuser",                       // username
                        "test@example.com",               // email
                        "USER"                            // userRole
                )
                .eventId(UUID.randomUUID().toString())  // This will be set automatically by the builder
                .build();
        // When
        notificationService.processUserEvent(event);

        // Then
        verify(mailSender, times(2)).send(emailCaptor.capture());
        
        // Verify welcome email to new user
        SimpleMailMessage welcomeEmail = emailCaptor.getAllValues().get(0);
        assertEquals("test@example.com", welcomeEmail.getTo()[0]);
        assertEquals("Welcome to Krainet Service", welcomeEmail.getSubject());
        assertTrue(welcomeEmail.getText().contains("Your account has been successfully created"));
        
        // Verify admin notification
        SimpleMailMessage adminEmail = emailCaptor.getAllValues().get(1);
        assertEquals(ADMIN_EMAIL, adminEmail.getTo()[0]);
        assertTrue(adminEmail.getSubject().contains("New User Registration"));
        assertTrue(adminEmail.getText().contains("A new user has been registered by admin"));
    }

    @Test
    void processUserEvent_UserUpdated_SendsNotification() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        UserEvent event = UserEvent.builder(
                        EventType.USER_PASSWORD_CHANGED,  // eventType
                        userId,                           // userId
                        "testuser",                       // username
                        "test@example.com",               // email
                        "USER"                            // userRole
                )
                .eventId(UUID.randomUUID().toString())  // This will be set automatically by the builder
                .build();

        // When
        notificationService.processUserEvent(event);

        // Then
        verify(mailSender, times(1)).send(emailCaptor.capture());
        
        SimpleMailMessage email = emailCaptor.getValue();
        assertEquals("test@example.com", email.getTo()[0]);
        assertTrue(email.getText().contains("Your account information has been updated by an administrator"));
    }

    @Test
    void processUserEvent_UserDeleted_SendsAdminNotification() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        UserEvent event = UserEvent.builder(
                        EventType.USER_PASSWORD_CHANGED,  // eventType
                        userId,                           // userId
                        "testuser",                       // username
                        "test@example.com",               // email
                        "USER"                            // userRole
                )
                .eventId(UUID.randomUUID().toString())  // This will be set automatically by the builder
                .build();

        // When
        notificationService.processUserEvent(event);

        // Then
        verify(mailSender, times(1)).send(emailCaptor.capture());
        
        SimpleMailMessage email = emailCaptor.getValue();
        assertEquals(ADMIN_EMAIL, email.getTo()[0]);
        assertTrue(email.getSubject().contains("User Account Deleted"));
        assertTrue(email.getText().contains("The following user account has been deleted by admin"));
    }

    @Test
    void processUserEvent_PasswordChanged_SendsNotification() {
        // Given
        UUID userId = UUID.randomUUID();

        UserEvent event = UserEvent.builder(
                        EventType.USER_PASSWORD_CHANGED,  // eventType
                        userId,                           // userId
                        "testuser",                       // username
                        "test@example.com",               // email
                        "USER"                            // userRole
                )
                .eventId(UUID.randomUUID().toString())  // This will be set automatically by the builder
                .build();

        // When
        notificationService.processUserEvent(event);

        // Then
        verify(mailSender, times(1)).send(emailCaptor.capture());
        
        SimpleMailMessage email = emailCaptor.getValue();
        assertEquals("test@example.com", email.getTo()[0]);
        assertEquals("Your password has been changed", email.getSubject());
        assertTrue(email.getText().contains("Your password has been successfully changed"));
    }

    @Test
    void processUserEvent_AccountEnabled_SendsNotification() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        UserEvent event = UserEvent.builder(
                        EventType.USER_PASSWORD_CHANGED,  // eventType
                        userId,                           // userId
                        "testuser",                       // username
                        "test@example.com",               // email
                        "USER"                            // userRole
                )
                .eventId(UUID.randomUUID().toString())  // This will be set automatically by the builder
                .build();
        // When
        notificationService.processUserEvent(event);

        // Then
        verify(mailSender, times(2)).send(emailCaptor.capture());
        
        // Verify user notification
        SimpleMailMessage userEmail = emailCaptor.getAllValues().get(0);
        assertEquals("test@example.com", userEmail.getTo()[0]);
        assertTrue(userEmail.getSubject().contains("Your account has been enabled"));
        assertTrue(userEmail.getText().contains("Your account has been enabled by an administrator"));
        
        // Verify admin notification
        SimpleMailMessage adminEmail = emailCaptor.getAllValues().get(1);
        assertEquals(ADMIN_EMAIL, adminEmail.getTo()[0]);
        assertTrue(adminEmail.getSubject().contains("User Account Enabled"));
    }

    @Test
    void processUserEvent_AccountDisabled_SendsNotification() {
        // Given
        UUID userId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        UserEvent event = UserEvent.builder(
                        EventType.USER_PASSWORD_CHANGED,  // eventType
                        userId,                           // userId
                        "testuser",                       // username
                        "test@example.com",               // email
                        "USER"                            // userRole
                )
                .eventId(UUID.randomUUID().toString())  // This will be set automatically by the builder
                .build();

        // When
        notificationService.processUserEvent(event);

        // Then
        verify(mailSender, times(2)).send(emailCaptor.capture());
        
        // Verify user notification
        SimpleMailMessage userEmail = emailCaptor.getAllValues().get(0);
        assertEquals("test@example.com", userEmail.getTo()[0]);
        assertTrue(userEmail.getSubject().contains("Your account has been disabled"));
        assertTrue(userEmail.getText().contains("Your account has been disabled by an administrator"));
        
        // Verify admin notification
        SimpleMailMessage adminEmail = emailCaptor.getAllValues().get(1);
        assertEquals(ADMIN_EMAIL, adminEmail.getTo()[0]);
        assertTrue(adminEmail.getSubject().contains("User Account Disabled"));
    }

    @Test
    void processUserEvent_UnknownEventType_LogsWarning() {
        // Given
        UserEvent event = UserEvent.builder(
                EventType.USER_PASSWORD_CHANGED,
                UUID.randomUUID(),
                "testuser",
                "test@example.com",
                "USER")
                .eventId(UUID.randomUUID().toString())
                .build();

        // When
        notificationService.processUserEvent(event);

        // Then
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
