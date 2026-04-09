package org.example.kafkaapplication.Producer;

import org.example.kafkaapplication.Model.User;
import org.example.kafkaapplication.PayLoad_DTO.EventType;
import org.example.kafkaapplication.PayLoad_DTO.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * The Kafka Producer logic
 */
@Service
public class KafkaProducer {

    private final KafkaTemplate<String, Event> kafkaTemplate;

    @Autowired
    public KafkaProducer(KafkaTemplate<String, Event> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void Notify(User user, EventType eventType) {
        String correlationId = UUID.randomUUID().toString();
        String eventId = eventType.name() + ":" + user.getUserId();
        // ORDER:sre1
        // Signup:sre1

        // interface -> notify(Object obj, EventType type);

        Map<String, User> payload = Map.of(user.getUserId(), user);
        // Temporal as Wrapper -> calls notify(User user, EventType type);
        kafkaTemplate.send(eventType.getTopic(),
                new Event(eventId, correlationId, eventType, payload, LocalDateTime.now()));
    }

}
