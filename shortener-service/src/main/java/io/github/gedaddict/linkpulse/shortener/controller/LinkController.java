package io.github.gedaddict.linkpulse.shortener.controller;

import io.github.gedaddict.linkpulse.common.ClickEvent;
import io.github.gedaddict.linkpulse.shortener.config.KafkaPublisher;
import io.github.gedaddict.linkpulse.shortener.dto.CreateLinkRequest;
import io.github.gedaddict.linkpulse.shortener.dto.CreateLinkResponse;
import io.github.gedaddict.linkpulse.shortener.entity.Link;
import io.github.gedaddict.linkpulse.shortener.service.LinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/link")
public class LinkController {

    private final LinkService linkService;
    private final KafkaPublisher publisher;

    @PostMapping
    public ResponseEntity<CreateLinkResponse> createLink(@RequestBody CreateLinkRequest request) {
        Link link = linkService.create(request.url());
        return ResponseEntity.ok()
                .body(new CreateLinkResponse(
                        link.getShortCode(),
                        link.getShortUrl(),
                        link.getOriginalUrl())
                );
    }

    @GetMapping
    public ResponseEntity<List<CreateLinkResponse>> getAllLinks() {
        List<Link> links = linkService.getAllLinks();
        List<CreateLinkResponse> linksResponse = new ArrayList<>();
        for (Link link : links) {
            linksResponse.add(new CreateLinkResponse(
                    link.getShortCode(),
                    link.getShortUrl(),
                    link.getOriginalUrl()));
        }
        return ResponseEntity.ok()
                .body(linksResponse);
    }

    @GetMapping("/{code}")
    public ResponseEntity<CreateLinkResponse> getLink(@PathVariable String code) {
        Link link = linkService.getLink(code);
        publisher.publish(new ClickEvent(
                link.getShortCode(),
                Instant.now(),
                HttpHeaders.USER_AGENT,
                HttpHeaders.REFERER)
        );
        return ResponseEntity.ok()
                .body(new CreateLinkResponse(
                        link.getShortCode(),
                        link.getShortUrl(),
                        link.getOriginalUrl())
                );
    }

    @DeleteMapping("/{code}")
    public ResponseEntity deleteLink(@PathVariable String code) {
        linkService.deleteLink(code);
        return ResponseEntity.ok()
                .build();
    }

}
