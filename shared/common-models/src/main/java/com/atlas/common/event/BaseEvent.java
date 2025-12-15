package com.atlas.common.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base class for all domain events in the system.
 * Provides common event metadata for tracing and idempotency.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEvent implements Serializable {

    /**
     * Unique identifier for this event instance.
     */
    private UUID eventId;

    /**
     * Type/name of the event for routing.
     */
    private String eventType;

    /**
     * Timestamp when the event occurred.
     */
    private LocalDateTime timestamp;

    /**
     * ID of the aggregate that produced this event.
     */
    private UUID aggregateId;

    /**
     * Version of the aggregate at the time of event.
     */
    private Long aggregateVersion;

    /**
     * Correlation ID for distributed tracing.
     */
    private String correlationId;

    /**
     * Source service that produced the event.
     */
    private String source;

    /**
     * Initialize event with default values.
     */
    protected void initializeEvent(String eventType, UUID aggregateId, String source) {
        this.eventId = UUID.randomUUID();
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
        this.aggregateId = aggregateId;
        this.source = source;
    }
}
