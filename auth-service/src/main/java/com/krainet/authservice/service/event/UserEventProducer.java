package com.krainet.authservice.service.event;

import com.krainet.authservice.model.User;
import com.krainet.common.event.EventType;
import com.krainet.common.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service responsible for producing user-related events to Kafka.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserEventProducer {

    private final KafkaTemplate<String, UserEvent> kafkaTemplate;
    private final String userEventsTopic;

    /**
     * Publishes a user event to Kafka asynchronously.
     *
     * @param eventType the type of user event
     * @param user the user this event is about
     */
    @Async
    public void publishUserEvent(EventType eventType, User user) {
        try {
            // Get the current authenticated user (initiator)
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UUID initiatorId = null;
            String initiatorUsername = null;

            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
                Object principal = authentication.getPrincipal();
                if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
                    org.springframework.security.core.userdetails.UserDetails userDetails =
                            (org.springframework.security.core.userdetails.UserDetails) principal;
                    initiatorUsername = userDetails.getUsername();
                    // If the user object has an ID, you can extract it here
                    // This assumes your UserDetails implementation has a method to get the ID
                    if (userDetails instanceof org.springframework.security.core.userdetails.User) {
                        // Adjust this based on how you store user ID in your UserDetails
                        initiatorId = UUID.fromString(userDetails.getUsername()); // This is just an example
                    }
                }
            }

            // Build the event
            UserEvent event = UserEvent.builder(
                            eventType,
                            user.getId(),
                            user.getUsername(),
                            user.getEmail(),
                            user.getRole().name(),
                            initiatorId,
                            initiatorUsername)
                    .build();

            // Send the event to Kafka
            kafkaTemplate.send(userEventsTopic, user.getId().toString(), event)
                    .whenComplete((result, ex) -> {
                        if (ex == null) {
                            log.debug("Successfully published {} event for user {}", eventType, user.getUsername());
                        } else {
                            log.error("Failed to publish {} event for user {}", eventType, user.getUsername(), ex);
                        }
                    });
            
            log.info("Published {} event for user {}", eventType, user.getUsername());
            
        } catch (Exception e) {
            log.error("Error publishing {} event for user {}: {}", eventType, user.getUsername(), e.getMessage(), e);
        }
    }
}
