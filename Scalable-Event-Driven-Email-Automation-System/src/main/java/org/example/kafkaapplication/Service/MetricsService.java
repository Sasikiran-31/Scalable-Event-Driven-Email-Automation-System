package org.example.kafkaapplication.Service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class MetricsService {
    private final AtomicLong receivedCount = new  AtomicLong();
    private final AtomicLong processedCount = new  AtomicLong();
    private final AtomicLong skippedCount = new  AtomicLong();
    private final AtomicLong failedCount = new  AtomicLong();


    public void recordReceived() {
        receivedCount.incrementAndGet();
    }

    public void recordProcessed() {
        processedCount.incrementAndGet();
    }
    public void recordSkipped() {
        skippedCount.incrementAndGet();
    }

    public void recordDlq() {
        failedCount.incrementAndGet();
    }

    public Map<String, Long> getMetrics() {
        return Map.of(
                "Events received: ", receivedCount.get(),
                "Events processed: ", processedCount.get(),
                "Duplicate Events: ", skippedCount.get(),
                "Failed Events: ", failedCount.get()
        );
    }
}
