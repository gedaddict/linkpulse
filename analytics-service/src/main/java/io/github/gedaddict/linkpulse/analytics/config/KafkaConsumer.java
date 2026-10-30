package io.github.gedaddict.linkpulse.analytics.config;

import io.github.gedaddict.linkpulse.analytics.entity.ClickRecord;
import io.github.gedaddict.linkpulse.analytics.repository.ClickRepository;
import io.github.gedaddict.linkpulse.common.ClickEvent;
import io.github.gedaddict.linkpulse.common.Topics;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ClickRepository clickRepository;

    @KafkaListener(topics = Topics.CLICK_EVENTS)
    public void record(ClickEvent event) {
        clickRepository.save(new ClickRecord(
                event.shortCode(),
                event.clickedAt(),
                event.userAgent(),
                event.referrer())
        );
    }
}