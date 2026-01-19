package com.example.demo;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic warehouseResponsesTopic() {
        return TopicBuilder.name("warehouse-responses")
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic warehouseDataTopic() {
        return TopicBuilder.name("warehouse-data-v2")
                .partitions(1)
                .replicas(1)
                .build();
    }
}