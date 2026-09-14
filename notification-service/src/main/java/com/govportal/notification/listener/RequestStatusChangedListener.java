package com.govportal.notification.listener;

import com.govportal.notification.event.RequestStatusChangedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RequestStatusChangedListener {

    @KafkaListener(topics = "request-status-changed", groupId = "notification-service")
    public void onStatusChanged(RequestStatusChangedEvent event) {
        // In production this would call an email/SMS provider. For this reference
        // implementation we log the notification that *would* be sent so the event
        // flow (create/update -> Kafka -> consumer) is fully observable end-to-end.
        if (event.previousStatus() == null) {
            log.info("NOTIFY citizen={} : your \"{}\" request {} has been received and is {}",
                    event.citizenUsername(), event.category(), event.requestId(), event.newStatus());
        } else {
            log.info("NOTIFY citizen={} : your \"{}\" request {} moved from {} to {}",
                    event.citizenUsername(), event.category(), event.requestId(),
                    event.previousStatus(), event.newStatus());
        }
    }
}
