package org.example.kafkaapplication;

import org.apache.kafka.clients.admin.NewTopic;
import org.example.kafkaapplication.Model.CustomerType;
import org.example.kafkaapplication.Model.User;
import org.example.kafkaapplication.PayLoad_DTO.EventType;
import org.example.kafkaapplication.Producer.KafkaProducer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootApplication
public class KafkaApplication {


    public static void main(String[] args) {
        SpringApplication.run(KafkaApplication.class, args);

    }

    @Bean
    @Profile("!test")
    CommandLineRunner throughputTest(KafkaProducer producer) {
        return args -> {

            int totalMessages = 100;   // load size
            int concurrency = 10;           // number of producer threads

            try (ExecutorService executor = Executors.newFixedThreadPool(concurrency)) {

                CountDownLatch latch =
                        new CountDownLatch(totalMessages);

                long start = System.currentTimeMillis();

                for (int i = 0; i < totalMessages; i++) {

                    executor.submit(() -> {

                        try {
                            User user = new User(
                                    "sas",
                                    "sasi",
                                    CustomerType.FREE
                            );

                            producer.Notify(user, EventType.ORDER);

                        } finally {
                            latch.countDown();
                        }

                    });
                }

                latch.await();

                long end = System.currentTimeMillis();

                long durationMs = end - start;

                double throughput =
                        (totalMessages*3 * 1000.0) / durationMs;

                System.out.println("------ THROUGHPUT TEST ------");
                System.out.println("Messages: " + totalMessages);
                System.out.println("Threads: " + concurrency);
                System.out.println("Time(ms): " + durationMs);
                System.out.println("Throughput(msg/sec): " + throughput);

                executor.shutdown();
            }
        };
    }



}
