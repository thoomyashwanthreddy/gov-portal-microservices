package com.govportal.notification.listener;

import com.govportal.notification.event.RequestStatusChangedEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;

class RequestStatusChangedListenerTest {

    private final RequestStatusChangedListener listener = new RequestStatusChangedListener();

    @Test
    void onStatusChanged_newRequest_doesNotThrow() {
        RequestStatusChangedEvent event = new RequestStatusChangedEvent(
                "req-1", "jane.citizen", "Pothole", null, "SUBMITTED", "jane.citizen", Instant.now());

        assertThatCode(() -> listener.onStatusChanged(event)).doesNotThrowAnyException();
    }

    @Test
    void onStatusChanged_statusTransition_doesNotThrow() {
        RequestStatusChangedEvent event = new RequestStatusChangedEvent(
                "req-1", "jane.citizen", "Pothole", "SUBMITTED", "APPROVED", "worker.bob", Instant.now());

        assertThatCode(() -> listener.onStatusChanged(event)).doesNotThrowAnyException();
    }
}
