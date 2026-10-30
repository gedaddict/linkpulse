package io.github.gedaddict.linkpulse.analytics.controller;

import io.github.gedaddict.linkpulse.analytics.dto.StatsResponse;
import io.github.gedaddict.linkpulse.analytics.entity.ClickRecord;
import io.github.gedaddict.linkpulse.analytics.service.ClickService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
public class StatsController {

    private final ClickService clickService;

    @GetMapping("/{code}")
    public ResponseEntity<StatsResponse> getStats(@PathVariable String code) {
        ClickRecord clickRecord = clickService.getStats(code);
        Integer total = clickService.getTotal(code);
        return ResponseEntity.ok()
                .body(new StatsResponse(
                        clickRecord.getShortCode(),
                        clickRecord.getClickedAt(),
                        total)
                );
    }
}