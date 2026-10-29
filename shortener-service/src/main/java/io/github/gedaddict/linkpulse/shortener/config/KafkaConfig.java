package io.github.gedaddict.linkpulse.shortener.config;

import io.github.gedaddict.linkpulse.common.Topics;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic clickEventsTopic() {
        return TopicBuilder.name(Topics.CLICK_EVENTS)
                .partitions(3)
                .replicas(1)
                .build();
    }
}