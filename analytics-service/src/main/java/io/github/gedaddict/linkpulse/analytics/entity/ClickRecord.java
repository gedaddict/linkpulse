package io.github.gedaddict.linkpulse.analytics.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Data
@NoArgsConstructor
@Table(name = "click_records")
public class ClickRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String shortCode;

    Instant clickedAt;

    String userAgent;

    String referrer;

    public ClickRecord(String shortCode, Instant clickedAt, String userAgent, String referrer) {
        this.shortCode = shortCode;
        this.clickedAt = clickedAt;
        this.userAgent = userAgent;
        this.referrer = referrer;
    }
}