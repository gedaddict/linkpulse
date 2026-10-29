package io.github.gedaddict.linkpulse.shortener.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@Table(name = "Links")
public class Link {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    String shortCode;

    String originalUrl;

    String shortUrl;

    Instant createdAt = Instant.now();

    public Link(String shortCode, String originalUrl, String shortUrl) {
        this.shortCode = shortCode;
        this.originalUrl = originalUrl;
        this.shortUrl = shortUrl;
    }
}
