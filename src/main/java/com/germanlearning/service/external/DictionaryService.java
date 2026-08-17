package com.germanlearning.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.germanlearning.dto.WordInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Word Explorer's source: German Wiktionary through the standard MediaWiki
 * action API.
 *
 * Uses {@code action=parse&prop=wikitext}, which is stable and documented,
 * rather than the experimental English-only structured definition endpoint.
 * Results are cached because dictionary entries change rarely and learners
 * click the same words repeatedly.
 */
@Service
public class DictionaryService {

    private static final Logger log = LoggerFactory.getLogger(DictionaryService.class);
    private static final Duration CACHE_TTL = Duration.ofHours(24);
    private static final int MAX_WORD_LENGTH = 60;

    private final RestClient restClient;
    private final ExternalApiProperties properties;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public DictionaryService(RestClient externalRestClient, ExternalApiProperties properties) {
        this.restClient = externalRestClient;
        this.properties = properties;
    }

    public WordInfoDto lookup(String rawWord) {
        String word = normalizeWord(rawWord);
        String pageUrl = "https://de.wiktionary.org/wiki/"
                + URLEncoder.encode(word, StandardCharsets.UTF_8);

        if (word.isEmpty() || !properties.getDictionary().isEnabled()) {
            return WordInfoDto.notFound(word, pageUrl);
        }

        CacheEntry cached = cache.get(word);
        if (cached != null && cached.isFresh()) {
            return cached.info();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(properties.getDictionary().getBaseUrl())
                    .queryParam("action", "parse")
                    .queryParam("page", word)
                    .queryParam("prop", "wikitext")
                    .queryParam("redirects", "1")
                    .queryParam("format", "json")
                    .queryParam("formatversion", "2")
                    .build()
                    .toUriString();

            JsonNode response = restClient.get().uri(url).retrieve().body(JsonNode.class);

            // MediaWiki reports a missing page as an error object, not a 404
            if (response == null || response.hasNonNull("error")) {
                return remember(word, WordInfoDto.notFound(word, pageUrl));
            }

            String wikitext = response.path("parse").path("wikitext").asText(null);
            return remember(word, WiktionaryParser.parse(word, wikitext, pageUrl));
        } catch (Exception e) {
            log.warn("Wiktionary lookup for '{}' failed: {}", word, e.toString());
            // Not cached: a network blip should not hide the word for a day
            return WordInfoDto.notFound(word, pageUrl);
        }
    }

    /** Words arrive from clicked text, so strip punctuation and stray markup. */
    private String normalizeWord(String rawWord) {
        if (rawWord == null) {
            return "";
        }
        String word = rawWord.trim()
                .replaceAll("^[^\\p{L}]+", "")
                .replaceAll("[^\\p{L}\\-]+$", "");
        return word.length() > MAX_WORD_LENGTH ? word.substring(0, MAX_WORD_LENGTH) : word;
    }

    private WordInfoDto remember(String word, WordInfoDto info) {
        cache.put(word, new CacheEntry(info, Instant.now()));
        return info;
    }

    private record CacheEntry(WordInfoDto info, Instant fetchedAt) {
        boolean isFresh() {
            return Duration.between(fetchedAt, Instant.now()).compareTo(CACHE_TTL) < 0;
        }
    }
}
