package io.github.gedaddict.linkpulse.common;


import java.time.Instant;

public record ClickEvent(
        String shortCode,
        Instant clickedAt,
        String userAgent,
        String referrer
) {
}