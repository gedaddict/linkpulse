package io.github.gedaddict.linkpulse.shortener.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateLinkRequest(@NotBlank String url) {
}
