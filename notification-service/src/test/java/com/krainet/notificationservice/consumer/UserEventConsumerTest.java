package com.krainet.notificationservice.consumer;

import com.krainet.common.event.EventType;
import com.krainet.common.event.UserEvent;
import com.krainet.notificationservice.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventConsumerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private Acknowledgment acknowledgment;

    @InjectMocks
    private UserEventConsumer userEventConsumer;

    private UserEvent testEvent;

    @BeforeEach
    void setUp() {
        testEvent = UserEvent.builder(
                EventType.USER_CREATED,
                UUID.randomUUID(),
                "testuser",
                "test@example.com",
                "USER",
                UUID.randomUUID(),
                "admin"
        ).build();
    }

    @Test
    void consumeUserEvent_ValidEvent_ProcessesAndAcknowledges() {
        // When
        userEventConsumer.consumeUserEvent(testEvent, acknowledgment);

        // Then
        verify(notificationService, times(1)).processUserEvent(testEvent);
        verify(acknowledgment, times(1)).acknowledge();
    }

    @Test
    void consumeUserEvent_ServiceThrowsException_StillAcknowledges() {
        // Given
        doThrow(new RuntimeException("Test exception"))
                .when(notificationService).processUserEvent(any());

        // When
        try {
            userEventConsumer.consumeUserEvent(testEvent, acknowledgment);
        } catch (Exception e) {
            // Expected exception
        }

        // Then
        verify(notificationService, times(1)).processUserEvent(testEvent);
        verify(acknowledgment, times(1)).acknowledge();
    }
}
