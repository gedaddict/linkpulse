package io.github.gedaddict.linkpulse.shortener.repository;

import io.github.gedaddict.linkpulse.shortener.entity.Link;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LinkRespository extends JpaRepository<Link, Long> {

    Optional<Link> findByShortCode(String shortCode);
    boolean existsByShortCode(String shortCode);
}
