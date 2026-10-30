package io.github.gedaddict.linkpulse.analytics.service;

import io.github.gedaddict.linkpulse.analytics.entity.ClickRecord;
import io.github.gedaddict.linkpulse.analytics.repository.ClickRepository;
import io.github.gedaddict.linkpulse.common.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClickService {

    private final ClickRepository clickRepository;

    public ClickRecord getStats(String code) {
        return clickRepository.findFirstByShortCodeOrderByClickedAtDesc(code)
                .orElseThrow(() -> new NotFoundException("code not found"));
    }

    public Integer getTotal(String code) {
        return clickRepository.countByShortCode(code)
                .orElseThrow(() -> new NotFoundException("code not found"));
    }
}