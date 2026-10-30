package io.github.gedaddict.linkpulse.analytics.dto;

import java.time.Instant;

public record StatsResponse (String shortCode, Instant lastClickedAt, int total){
}