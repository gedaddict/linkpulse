package io.github.gedaddict.linkpulse.shortener.config;

import io.github.gedaddict.linkpulse.common.ClickEvent;
import io.github.gedaddict.linkpulse.common.Topics;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class KafkaPublisher {

    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;
    private final Counter published;
    private final Counter failed;

    public KafkaPublisher(KafkaTemplate<String, ClickEvent> kafkaTemplate, MeterRegistry meterRegistry) {
        this.kafkaTemplate = kafkaTemplate;
        this.published = Counter.builder("linkpulse.clicks.published")
                .description("Click event sent to kafka")
                .register(meterRegistry);
        this.failed = Counter.builder("linkpulse.clicks.failed")
                .description("Click event that failed to sent")
                .register(meterRegistry);
    }

    public void publish(ClickEvent event) {
        try {
            kafkaTemplate.send(Topics.CLICK_EVENTS, event.shortCode(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            failed.increment();
                            log.warn("Failed to publish click for {}", event.shortCode(), ex);
                        } else {
                            published.increment();
                        }
                    });
        } catch (Exception e) {
            log.warn("Could not send click for {}", event.shortCode(), e);
        }
    }
}