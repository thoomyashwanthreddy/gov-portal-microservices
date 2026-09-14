package com.govportal.notification.event;

import java.time.Instant;

/**
 * Mirrors com.govportal.event.RequestStatusChangedEvent produced by the backend.
 * Deliberately duplicated rather than shared via a common library, matching how
 * independently-deployed microservices in this architecture avoid a tight compile-time
 * coupling — the Kafka topic's JSON schema is the actual contract between them.
 */
public record RequestStatusChangedEvent(
        String requestId,
        String citizenUsername,
        String category,
        String previousStatus,
        String newStatus,
        String changedByUsername,
        Instant occurredAt
) {
}
