package org.example.kafkaapplication.Consumer;

import lombok.extern.slf4j.Slf4j;
import org.example.kafkaapplication.NotificationService.NotificationService;
import org.example.kafkaapplication.PayLoad_DTO.Event;
import org.example.kafkaapplication.Service.MetricsService;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.kafka.retrytopic.DltStrategy;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
public class KafkaListeners {

    @Autowired
    NotificationService notificationService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    MetricsService metricsService;

    /**
     * Main Consumer Logic
     * Satisfies NFRs: Reliability (At-least-once), Idempotency (Exactly-once email),
     * Throughput (Virtual Threads via Container Factory), and Observability (Correlation IDs).
     */
    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 1000, multiplier = 2.0),
            dltStrategy = DltStrategy.FAIL_ON_ERROR
    )
    @KafkaListener(id = "email-notifier", topics = {"user.signup", "order.completed", "trial.expiring"})
    public void consume(Event event, @Header(KafkaHeaders.RECEIVED_PARTITION) int partition, Acknowledgment ack) {

        // 1. Trace the event lifecycle using the Correlation ID
        try (var mdc = MDC.putCloseable("correlationId", event.correlationId())) {
            log.info("Event received. Checking Idempotency for ID: {}", event.eventId());
            metricsService.recordReceived();

            // 2. Idempotency Check: Ensure same event_id never triggers >1 email
            Boolean isNew = redisTemplate.opsForValue()
                    .setIfAbsent(event.eventId(), "1", Duration.ofHours(3));

            if (Boolean.FALSE.equals(isNew)) {
                log.warn("Duplicate event detected. Skipping EventId: {}", event.eventId());
                metricsService.recordSkipped();
                ack.acknowledge(); // Acknowledge so Kafka doesn't keep redelivering the duplicate
                return;
            }

            try {
                // 3. Simulated Email Dispatch
                notificationService.sendEmail(event);

                log.info("Successfully processed event: {}", event.event_type());
                metricsService.recordProcessed();

                // 4. Manual Offset Commit: Only after successful processing
                ack.acknowledge();

            } catch (Exception e) {
                // 5. CRITICAL: Release the Redis lock if processing fails.
                // This ensures the @RetryableTopic attempt can actually run instead of being skipped as a "duplicate".
                redisTemplate.delete(event.eventId());

                log.error("Failed to process event. ID: {}. Lock released for retry. Reason: {}",
                        event.eventId(), e.getMessage());

                // Re-throw to trigger Kafka Retry logic
                throw e;
            }
        }
    }

    /**
     * Dead Letter Queue (DLQ) Handler
     * Preserves failure metadata and ensures observability of unrecoverable errors.
     */
    @DltHandler
    public void handleDlt(Event event, @Header(KafkaHeaders.EXCEPTION_MESSAGE) String exceptionMessage) {
        try (var mdc = MDC.putCloseable("correlationId", event.correlationId())) {
            log.error("Event exhausted retries and moved to DLQ. EventId: {} | Final Reason: {}",
                    event.eventId(), exceptionMessage);

            metricsService.recordDlq();

            // Ensure the lock is deleted so the DLQ Replay mechanism can eventually succeed
            redisTemplate.delete(event.eventId());
            log.info("Idempotency lock cleared for DLQ event: {}", event.eventId());
        }
    }
}