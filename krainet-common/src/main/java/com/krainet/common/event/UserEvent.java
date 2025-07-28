package com.krainet.common.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Represents a user-related event that can be published to Kafka.
 */
@Data
@Builder(builderMethodName = "hiddenBuilder")
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserEvent {
    /**
     * Unique identifier for the event
     */
    private String eventId;
    
    /**
     * Type of the event
     */
    private EventType eventType;
    
    /**
     * Timestamp when the event occurred
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS")
    private LocalDateTime timestamp;
    
    /**
     * ID of the user this event is about
     */
    private UUID userId;
    
    /**
     * Username of the user this event is about
     */
    private String username;
    
    /**
     * Email of the user this event is about
     */
    private String email;
    
    /**
     * Role of the user this event is about
     */
    private String userRole;
    
    /**
     * ID of the user who initiated this event (if applicable)
     */
    private UUID initiatorUserId;
    
    /**
     * Username of the user who initiated this event (if applicable)
     */
    private String initiatorUsername;
    
    /**
     * Additional metadata about the event
     */
    private Map<String, Object> metadata;
    
    /**
     * Creates a new UserEvent with the given type and user details.
     *
     *
     * @return a new UserEvent instance
     */
    private static UserEventBuilder builder() {
        return new UserEventBuilder()
                .eventId(UUID.randomUUID().toString())
                .timestamp(LocalDateTime.now());
    }
    
    public static UserEventBuilder builder(EventType eventType, UUID userId, String username, String email, String userRole) {
        return builder()
                .eventType(eventType)
                .userId(userId)
                .username(username)
                .email(email)
                .userRole(userRole);
    }
    
    /**
     * Creates a new UserEvent with the given type, user details, and initiator details.
     *
     * @param eventType the type of event
     * @param userId the ID of the user this event is about
     * @param username the username of the user
     * @param email the email of the user
     * @param userRole the role of the user
     * @param initiatorUserId the ID of the user who initiated this event
     * @param initiatorUsername the username of the user who initiated this event
     * @return a new UserEvent instance
     */
    public static UserEventBuilder builder(
            EventType eventType, 
            UUID userId, String username, 
            String email, String userRole,
            UUID initiatorUserId, String initiatorUsername) {
        
        return builder(eventType, userId, username, email, userRole)
                .initiatorUserId(initiatorUserId)
                .initiatorUsername(initiatorUsername);
    }
}
