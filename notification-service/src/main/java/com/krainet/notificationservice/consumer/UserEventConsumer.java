package com.krainet.notificationservice.consumer;

import com.krainet.common.event.UserEvent;
import com.krainet.notificationservice.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Kafka consumer for user-related events.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserEventConsumer {

    private final NotificationService notificationService;

    /**
     * Listens for user events from Kafka and processes them.
     *
     * @param event the user event
     * @param ack the acknowledgment to commit the offset
     */
    @KafkaListener(
            topics = "#{@userEventsTopic}",
            groupId = "#{'${kafka.consumer.group-id:notification-service}'}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumeUserEvent(@Payload UserEvent event, Acknowledgment ack) {
        try {
            log.debug("Received user event: {} for user {}", event.getEventType(), event.getUsername());
            
            // Process the event
            notificationService.processUserEvent(event);
            
            // Acknowledge the message
            ack.acknowledge();
            log.debug("Successfully processed user event: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Error processing user event: {}", event.getEventId(), e);
            // The message will be retried based on the retry policy
            // Consider implementing dead-letter queue for failed messages
            throw e;
        }
    }
}
