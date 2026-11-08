package io.github.gedaddict.linkpulse.analytics;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;

import io.github.gedaddict.linkpulse.analytics.repository.ClickRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@SpringBootTest(properties = "spring.kafka.listener.auto-startup=false")
@Testcontainers
@Transactional
class PostgresQueryIT {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16");

    @Autowired JdbcTemplate jdbc;
    @Autowired ClickRepository repository; // your repository

    private void insertClick(long id, String code, OffsetDateTime at) {
        jdbc.update("INSERT INTO click_records (id, short_code, clicked_at) VALUES (?, ?, ?)",
                id, code, at);
    }

    @Test
    void countsClicksOnlyForRequestedShortCode() {
        OffsetDateTime now = OffsetDateTime.now();
        insertClick(900001, "abc", now);
        insertClick(900002, "abc", now.minusDays(1));
        insertClick(900003, "xyz", now);

        assertThat(repository.countByShortCode("abc").get()).isEqualTo(2);
        assertThat(repository.countByShortCode("xyz").get()).isEqualTo(1);
        assertThat(repository.countByShortCode("none").get()).isEqualTo(0);
    }
}