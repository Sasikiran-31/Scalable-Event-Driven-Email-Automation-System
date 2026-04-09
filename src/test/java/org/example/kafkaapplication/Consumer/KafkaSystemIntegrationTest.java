package org.example.kafkaapplication.Consumer;

import org.example.kafkaapplication.BaseIntegrationTest;
import org.example.kafkaapplication.Model.CustomerType;
import org.example.kafkaapplication.Model.User;
import org.example.kafkaapplication.NotificationService.NotificationService;
import org.example.kafkaapplication.PayLoad_DTO.Event;
import org.example.kafkaapplication.PayLoad_DTO.EventType;
import org.example.kafkaapplication.Producer.KafkaProducer;
import org.example.kafkaapplication.Service.MetricsService;
import org.example.kafkaapplication.Controller.DlqController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
class KafkaSystemIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Event> kafkaTemplate;

    @Autowired
    private KafkaProducer producer;

    @Autowired
    private DlqController dlqController;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    MetricsService metricsService;

    /**
     * TEST 1: IDEMPOTENCY
     * Verifies that duplicate Kafka messages do not trigger multiple emails.
     */
    @Test
    void shouldProcessOnlyOnce_WhenDuplicateEventReceived() throws InterruptedException {
        String eventId = "idempotency-test-" + UUID.randomUUID(); // Unique ID
        User user = new User("test@datumart.com", "Tester", CustomerType.PRO);
        Event event = new Event(eventId, "trace-1", EventType.ORDER, Map.of(String.valueOf(1L), user), LocalDateTime.now());

        doNothing().when(notificationService).sendEmail(any());

        kafkaTemplate.send("order.completed", eventId, event);
        kafkaTemplate.send("order.completed", eventId, event);

        // Verify exactly one call despite two sends
        verify(notificationService, timeout(10000).times(1)).sendEmail(any());
    }

    /**
     * TEST 2: RESILIENCE & REPLAY
     * Verifies Failure -> Retry -> DLQ -> Manual Replay -> Success
     */
    @Test
    void shouldFlowFromFailureToDlqToSuccessfulReplay() throws InterruptedException {
        // Step 1: Force Failure
        doThrow(new RuntimeException("Simulated SMTP Failure"))
                .when(notificationService).sendEmail(any());

        User user = new User("recovery@datumart.com", "RecoveryUser", CustomerType.FREE);
        producer.Notify(user, EventType.SIGNUP);

        // Wait for 3 failure attempts (original + 2 retries)
        verify(notificationService, timeout(15000).times(3)).sendEmail(any());

        // Step 2: Fix the service (Reset mock)
        reset(notificationService);
        doNothing().when(notificationService).sendEmail(any());

        // Step 3: Trigger Replay
        dlqController.startReplay();

        // Step 4: Verify recovery success
        // Note: verify(times(1)) here is correct because we reset the mock counter
        verify(notificationService, timeout(10000).times(1)).sendEmail(any());

        dlqController.stopReplay();
    }

    /**
     * TEST 3: HiGH THROUGHPUT AND METRICS TEST
     * Verifies that duplicate Kafka messages do not trigger multiple emails.
     */
    @Test
    void highThroughputAndMetricsStressTest() throws InterruptedException {
        // 1. Arrange: Prepare a significant load
        int loadSize = 1000;
        doNothing().when(notificationService).sendEmail(any());

        // Get baseline to ensure we account for any previously processed events
        long initialProcessed = metricsService.getMetrics().getOrDefault("Events processed: ", 0L);

        // 2. Act: Hammer the producer
        // This tests the non-blocking nature of your Virtual Threads
        for (long i = 0; i < loadSize; i++) {
            User user = new User("stress" + i + "@test.com", "StressUser", CustomerType.FREE);
            producer.Notify(user, EventType.ORDER);
        }

        // 3. Assert: Poll until the MetricsService catches up
        // This proves the Consumers are effectively draining the Kafka topics
        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(200, TimeUnit.MILLISECONDS)
                .until(() -> {
                    long current = metricsService.getMetrics().getOrDefault("Events processed: ", 0L);
                    return current >= (initialProcessed + loadSize);
                });

        // 4. Final Validation
        Map<String, Long> finalMetrics = metricsService.getMetrics();
        long totalProcessed = finalMetrics.get("Events processed: ");

        assertTrue(totalProcessed >= initialProcessed + loadSize,
                "Metrics should reflect the full load of " + loadSize + " events");

        System.out.println("Stress Test Passed. Total Processed: " + totalProcessed);
    }
}