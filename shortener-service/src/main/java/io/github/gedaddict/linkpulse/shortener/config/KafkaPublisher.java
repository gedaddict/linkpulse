package io.github.gedaddict.linkpulse.shortener.config;

import io.github.gedaddict.linkpulse.common.ClickEvent;
import io.github.gedaddict.linkpulse.common.Topics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaPublisher {

    private final KafkaTemplate<String, ClickEvent> kafkaTemplate;

    public void publish(ClickEvent event) {
        try {
            kafkaTemplate.send(Topics.CLICK_EVENTS, event.shortCode(), event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.warn("Failed to publish click for {}", event.shortCode(), ex);
                        }
                    });
        } catch (Exception e) {
            log.warn("Could not send click for {}", event.shortCode(), e);
        }
    }
}