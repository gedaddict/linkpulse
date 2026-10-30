package io.github.gedaddict.linkpulse.analytics.repository;

import io.github.gedaddict.linkpulse.analytics.entity.ClickRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClickRepository extends JpaRepository<ClickRecord, Long> {

    Optional<ClickRecord> findByShortCode(String code);

    Optional<ClickRecord> findFirstByShortCodeOrderByClickedAtDesc(String code);

    Optional<Integer> countByShortCode(String shortCode);
}