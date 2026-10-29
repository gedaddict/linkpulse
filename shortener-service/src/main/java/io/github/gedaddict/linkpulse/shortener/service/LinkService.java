package io.github.gedaddict.linkpulse.shortener.service;

import io.github.gedaddict.linkpulse.shortener.entity.Link;
import io.github.gedaddict.linkpulse.shortener.exception.NotFoundException;
import io.github.gedaddict.linkpulse.shortener.repository.LinkRespository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class LinkService {

    private final LinkRespository linkRespository;

    private static final char[] ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYXabcdefghijklmnopqrstuvwxyz0123456789"
            .toCharArray();
    private static final int CODE_LENGHT = 3;

    @Transactional
    public Link create(String url) {
        String shortCode = createRandomCode();
        String baseUrl = parseBaseUrl(url);
        return linkRespository.save(new Link(shortCode, url, baseUrl + shortCode));
    }

    public List<Link> getAllLinks() {
        return linkRespository.findAll();
    }

    public Link getLink(String code) {
        return linkRespository.findByShortCode(code)
                .orElseThrow(() -> new NotFoundException("code not found"));
    }

    public void deleteLink(String code) {
        Link link = getLink(code);
        linkRespository.delete(link);
    }

    private String parseBaseUrl(String url) {
        try {
            URI uri = new URI(url);
            return uri.getScheme() + "://" + uri.getAuthority() + "/";
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private String createRandomCode() {
        StringBuilder sb = new StringBuilder(CODE_LENGHT);
        int value = new Random().nextInt(100000);
        while (value > 0) {
            sb.append(ALPHABET[value % 62]);
            value /= 62;
        }
        return sb.toString();
    }
}
