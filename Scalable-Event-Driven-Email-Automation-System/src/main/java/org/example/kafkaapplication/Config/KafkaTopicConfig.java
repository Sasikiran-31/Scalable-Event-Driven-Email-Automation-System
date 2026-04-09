package org.example.kafkaapplication.Config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;


/**
 * The configuration class for building Kafka topics
 * Here three topics are used for handling 3 different notification scenarios
 */
@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic signupTopic() {
        return TopicBuilder.name("user.signup")
                .partitions(4)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic orderTopic() {
        return TopicBuilder.name("order.completed")
                .partitions(4)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic trialExpiringTopic() {
        return TopicBuilder.name("trial.expiring")
                .partitions(4)
                .replicas(1)
                .build();
    }
}
