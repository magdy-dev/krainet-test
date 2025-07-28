package com.krainet.notificationservice.service;

import com.krainet.common.event.EventType;
import com.krainet.common.event.UserEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending notifications based on user events.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final JavaMailSender mailSender;
    private final String adminEmail = "admin@krainet.com"; // Should be configured in properties

    /**
     * Processes a user event and sends appropriate notifications.
     *
     * @param event the user event to process
     */
    public void processUserEvent(UserEvent event) {
        log.info("Processing user event: {} for user {}", event.getEventType(), event.getUsername());

        try {
            switch (event.getEventType()) {
                case USER_CREATED:
                    sendUserCreatedNotifications(event);
                    break;
                case USER_UPDATED:
                    sendUserUpdatedNotification(event);
                    break;
                case USER_DELETED:
                    sendUserDeletedNotification(event);
                    break;
                case USER_PASSWORD_CHANGED:
                    sendPasswordChangedNotification(event);
                    break;
                case USER_ACCOUNT_ENABLED:
                case USER_ACCOUNT_DISABLED:
                    sendAccountStatusNotification(event);
                    break;
                default:
                    log.warn("Unhandled event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing user event: {}", event.getEventId(), e);
            // Consider implementing retry logic or dead-letter queue for failed notifications
        }
    }

    /**
     * Sends a notification to the admin when a new user is created.
     */
    private void sendUserCreatedNotifications(UserEvent event) {
        // Send welcome email to the new user
        String userSubject = "Welcome to Krainet Service";
        String userMessage = String.format(
                "Hello %s,\n\n" +
                "Your account has been successfully created.\n" +
                "Username: %s\n" +
                "Email: %s\n\n" +
                "Thank you for joining us!",
                event.getUsername(),
                event.getUsername(),
                event.getEmail()
        );
        sendEmail(event.getEmail(), userSubject, userMessage);

        // Notify admin about new user
        if (event.getInitiatorUserId() != null && !event.getInitiatorUserId().equals(event.getUserId())) {
            String adminSubject = String.format("New User Registration: %s", event.getUsername());
            String adminMessage = String.format(
                    "A new user has been registered by %s.\n\n" +
                    "User Details:\n" +
                    "- Username: %s\n" +
                    "- Email: %s\n" +
                    "- Role: %s\n\n" +
                    "Registration Time: %s",
                    event.getInitiatorUsername() != null ? event.getInitiatorUsername() : "system",
                    event.getUsername(),
                    event.getEmail(),
                    event.getUserRole(),
                    event.getTimestamp()
            );
            sendEmail(adminEmail, adminSubject, adminMessage);
        }
    }

    /**
     * Sends a notification when a user's profile is updated.
     */
    private void sendUserUpdatedNotification(UserEvent event) {
        // Only notify if the update was done by an admin
        if (event.getInitiatorUserId() != null && !event.getInitiatorUserId().equals(event.getUserId())) {
            String subject = String.format("Your account has been updated by an administrator");
            String message = String.format(
                    "Hello %s,\n\n" +
                    "Your account information has been updated by an administrator.\n\n" +
                    "If you did not request these changes or believe this is an error, " +
                    "please contact our support team immediately.\n\n" +
                    "Best regards,\nThe Krainet Team",
                    event.getUsername()
            );
            sendEmail(event.getEmail(), subject, message);
        }
    }

    /**
     * Sends a notification when a user is deleted.
     */
    private void sendUserDeletedNotification(UserEvent event) {
        // Only send notification if the user was deleted by someone else (admin)
        if (event.getInitiatorUserId() != null && !event.getInitiatorUserId().equals(event.getUserId())) {
            String adminSubject = String.format("User Account Deleted: %s", event.getUsername());
            String adminMessage = String.format(
                    "The following user account has been deleted by %s.\n\n" +
                    "User Details:\n" +
                    "- Username: %s\n" +
                    "- Email: %s\n" +
                    "- Role: %s\n" +
                    "- Deletion Time: %s\n\n" +
                    "This action is irreversible.",
                    event.getInitiatorUsername(),
                    event.getUsername(),
                    event.getEmail(),
                    event.getUserRole(),
                    event.getTimestamp()
            );
            sendEmail(adminEmail, adminSubject, adminMessage);
        }
    }

    /**
     * Sends a notification when a user's password is changed.
     */
    private void sendPasswordChangedNotification(UserEvent event) {
        String subject = "Your password has been changed";
        String message = String.format(
                "Hello %s,\n\n" +
                "Your password has been successfully changed.\n\n" +
                "If you did not make this change, please contact our support team immediately.\n\n" +
                "Best regards,\nThe Krainet Team",
                event.getUsername()
        );
        sendEmail(event.getEmail(), subject, message);
    }

    /**
     * Sends a notification when a user's account status changes (enabled/disabled).
     */
    private void sendAccountStatusNotification(UserEvent event) {
        boolean isEnabled = event.getEventType() == EventType.USER_ACCOUNT_ENABLED;
        String status = isEnabled ? "enabled" : "disabled";
        
        // Notify the user
        String userSubject = String.format("Your account has been %s", status);
        String userMessage = String.format(
                "Hello %s,\n\n" +
                "Your account has been %s by an administrator.\n\n" +
                "%s\n\n" +
                "If you believe this is an error, please contact our support team.\n\n" +
                "Best regards,\nThe Krainet Team",
                event.getUsername(),
                status,
                isEnabled ? "You can now log in to your account." 
                         : "You will not be able to log in until an administrator re-enables your account."
        );
        sendEmail(event.getEmail(), userSubject, userMessage);
        
        // Notify admin
        if (event.getInitiatorUserId() != null && !event.getInitiatorUserId().equals(event.getUserId())) {
            String adminSubject = String.format("User Account %s: %s", 
                    isEnabled ? "Enabled" : "Disabled", event.getUsername());
            String adminMessage = String.format(
                    "The following user account has been %s by %s.\n\n" +
                    "User Details:\n" +
                    "- Username: %s\n" +
                    "- Email: %s\n" +
                    "- Role: %s\n" +
                    "- Time: %s\n\n" +
                    "Action taken by: %s (%s)",
                    status,
                    event.getInitiatorUsername(),
                    event.getUsername(),
                    event.getEmail(),
                    event.getUserRole(),
                    event.getTimestamp(),
                    event.getInitiatorUsername(),
                    event.getInitiatorUserId()
            );
            sendEmail(adminEmail, adminSubject, adminMessage);
        }
    }

    /**
     * Helper method to send an email.
     */
    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.debug("Email sent to {} with subject: {}", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage(), e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
}
