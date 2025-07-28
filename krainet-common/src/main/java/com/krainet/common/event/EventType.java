package com.krainet.common.event;

/**
 * Enum representing different types of user events that can be published to Kafka.
 */
public enum EventType {
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED,
    USER_PASSWORD_CHANGED,
    USER_ACCOUNT_ENABLED,
    USER_ACCOUNT_DISABLED,
    UNKNOWN
}
