package org.example.kafkaapplication.PayLoad_DTO;

import org.example.kafkaapplication.Model.User;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Event DTO
 */
public record Event(
        String eventId,
        String correlationId,
        EventType event_type,
        Map<String, User> payload,
        LocalDateTime timestamp

) {
    @Override
    @NonNull
    public String toString() {
        return "Event{" +
                "eventId='" + eventId + '\'' +
                ", eventType='" + event_type + '\'' +
                ", payload=" + payload + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }

    public User getUser(String id) {
        return payload.get(id);
    }
}
