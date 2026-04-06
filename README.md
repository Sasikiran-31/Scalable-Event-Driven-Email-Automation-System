# Scalable Event-Driven Email Automation System

This project is a high-performance email automation system built with **Spring Boot 3.4+**, **Apache Kafka**, and **Redis**. It is designed to handle high-throughput notification scenarios while ensuring reliability and data integrity through advanced distributed system patterns.

## 🚀 Key Features

* **High-Throughput Concurrency**: Leverages **Java 21 Virtual Threads** within the Kafka listener container to handle thousands of concurrent I/O-bound email dispatches with minimal memory overhead.
* **Exactly-Once Processing (Idempotency)**: Uses **Redis** as a distributed lock to ensure that duplicate Kafka events do not result in multiple emails being sent to the same user.
* **Self-Healing Resilience**: Implements a 3-attempt retry strategy with **exponential backoff** (1s base, 2.0 multiplier) for handling transient failures like SMTP timeouts.
* **Dead Letter Queue (DLQ) & Manual Replay**: Automatically moves unrecoverable messages to a DLT (Dead Letter Topic) and provides a service to manually replay them back to original topics.
* **Observability**: Integrated **MDC logging** with Correlation IDs for end-to-end event tracing and a **Metrics Service** for real-time monitoring of system activity.

## 🏗️ Architecture

* **Producer**: Generates a unique `eventId` and `correlationId` for every notification request to ensure traceability.
* **Topics**: Partitioned topics (`user.signup`, `order.completed`, `trial.expiring`) configured to enable horizontal scaling.
* **Consumer**: An acknowledgment-aware listener that manages Redis locks and manual offsets to guarantee at-least-once delivery without duplicates.
* **Email Templates**: Decoupled template logic for different event types, including Signups, Orders, and Trial Expirations.

## Project Structure
```
Scalable-Event-Driven-Email-Automation-System/
├── src/main/java/org/example/kafkaapplication/
│   ├── Config/
│   │   ├── KafkaConsumerConfig.java    # Virtual Thread & Manual Ack setup
│   │   └── KafkaTopicConfig.java       # Topic definitions (signup, order, trial)
│   ├── Consumer/
│   │   ├── KafkaListeners.java         # Main logic (Idempotency & Retries)
│   │   └── DlqReplayService.java       # Manual recovery from DLT topics
│   ├── Producer/
│   │   └── KafkaProducer.java          # Event generation with Correlation IDs
│   ├── NotificationService/
│   │   └── NotificationService.java    # Simulated SMTP dispatch logic
│   ├── Service/
│   │   └── MetricsService.java         # Real-time event tracking (AtomicLong)
│   ├── controller/
│   │   ├── DlqController.java          # API to start/stop DLQ replay
│   │   └── MetricsController.java       # API to view system health/stats
│   ├── EmailTemplates/                 # Strategy pattern for email content
│   │   ├── EmailTemplate.java
│   │   ├── SignupTemplate.java
│   │   ├── OrderTemplate.java
│   │   └── TrialExpirationTemplate.java
│   ├── Model/                          # Core Domain Entities (User, CustomerType)
│   └── PayLoad_DTO/                    # Data Transfer Objects (Event, EventType)
├── src/main/resources/
│   └── application.yaml                # Kafka & Redis connection settings
├── src/test/java/org/example/kafkaapplication/
│   ├── BaseIntegrationTest.java        # Testcontainers (Kafka/Redis) setup
│   └── Consumer/
│       └── KafkaSystemIntegrationTest.java # Idempotency & Stress tests
├── docker-compose.yml                  # Infrastructure orchestration
└── pom.xml                             # Project dependencies and build config
```

## 🛠️ Getting Started

### Prerequisites
* **Java 21** or **25**
* **Docker Desktop** (Required for Kafka, Redis, and Integration Tests)
* **Maven 3.9+**

### Build and Run
1.  **Build the executable JAR**:
    ```bash
    mvn clean package
    ```
2.  **Run with Docker Compose**:
    ```bash
    docker-compose up --build
    ```

### API Endpoints
* **Get System Metrics**: `GET /api/metrics`
* **Start DLQ Replay**: `POST /api/dlq/start`
* **Stop DLQ Replay**: `POST /api/dlq/stop`

## 🧪 Testing

The project includes a comprehensive integration suite that uses **Testcontainers** to verify system behavior in a real-world environment:
* **Idempotency**: Confirms duplicate Kafka messages are suppressed by the Redis layer.
* **Resilience**: Verifies that failed events correctly exhaust retries before moving to the DLQ.
* **DLQ Replay**: Validates the end-to-end recovery flow from DLT back to successful processing.
* **Stress Performance**: Proves the system maintains accuracy and throughput under high load.

Run the test suite using:
```bash
mvn test
