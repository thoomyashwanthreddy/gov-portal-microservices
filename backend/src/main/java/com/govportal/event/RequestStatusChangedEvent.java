package com.govportal.event;

import com.govportal.domain.RequestStatus;

import java.time.Instant;

/**
 * Published to the {@code request-status-changed} Kafka topic whenever a
 * service request is created or has its status updated. Consumed by the
 * standalone notification-service.
 */
public record RequestStatusChangedEvent(
        String requestId,
        String citizenUsername,
        String category,
        RequestStatus previousStatus,
        RequestStatus newStatus,
        String changedByUsername,
        Instant occurredAt
) {
    public static RequestStatusChangedEvent of(String requestId,
                                                String citizenUsername,
                                                String category,
                                                RequestStatus previousStatus,
                                                RequestStatus newStatus,
                                                String changedByUsername) {
        return new RequestStatusChangedEvent(
                requestId, citizenUsername, category, previousStatus, newStatus,
                changedByUsername, Instant.now()
        );
    }
}
