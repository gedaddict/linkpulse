package io.github.gedaddict.linkpulse.shortener.dto;

public record CreateLinkResponse(String code, String shortUrl, String originalUrl) {
}
