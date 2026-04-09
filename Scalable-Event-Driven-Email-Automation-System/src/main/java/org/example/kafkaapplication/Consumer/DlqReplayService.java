package org.example.kafkaapplication.Consumer;

import lombok.extern.slf4j.Slf4j;
import org.example.kafkaapplication.PayLoad_DTO.Event;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;


/**
 * This service is responsible for replaying the failed messages (manual triggering required) from the DLT queue.
 */
@Slf4j
@Service
public class DlqReplayService {

    private final KafkaTemplate<String, Event> kafkaTemplate;
    private final KafkaListenerEndpointRegistry registry;

    @Autowired
    public DlqReplayService(KafkaTemplate<String, Event> kafkaTemplate, KafkaListenerEndpointRegistry registry) {
        this.kafkaTemplate = kafkaTemplate;
        this.registry = registry;
    }

    // Listen to all DLT topics, but DO NOT start automatically
    @KafkaListener(id = "dlq-replayer",
            topics = {"user.signup-dlt", "order.completed-dlt", "trial.expiring-dlt"},
            autoStartup = "false")
    public void consumeFromDlq(Event event, @Header(KafkaHeaders.RECEIVED_TOPIC) String dltTopic) {

        // Remove the "-dlt" suffix to get the original topic name
        String originalTopic = dltTopic.replace("-dlt", "");

        log.info("Replaying event {} from {} back to {}", event.eventId(), dltTopic, originalTopic);

        // Push the event back to the original topic for a second chance
        kafkaTemplate.send(originalTopic, event.eventId(), event);
    }

    public void startReplay() {
        log.info("Starting DLQ Replay Listener...");
        registry.getListenerContainer("dlq-replayer").start();
    }

    public void stopReplay() {
        log.info("Stopping DLQ Replay Listener...");
        registry.getListenerContainer("dlq-replayer").stop();
    }
}