package com.germanlearning.service.external;

import com.fasterxml.jackson.databind.JsonNode;
import com.germanlearning.dto.ExampleSentenceDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Real German sentences from Tatoeba, used to enrich a lesson — never to
 * define it.
 *
 * Lesson content stays authored by LinguaLearn: this only fills the optional
 * "Real examples" activity. Everything that comes back is filtered before a
 * learner sees it (right language, sensible length, actually contains the word,
 * has an English translation, no leftover markup), because the corpus is
 * community written and quality varies. Failures are never fatal: a lesson with
 * no examples simply shows none.
 */
@Service
public class TatoebaService {

    private static final Logger log = LoggerFactory.getLogger(TatoebaService.class);
    private static final Duration CACHE_TTL = Duration.ofHours(6);

    private final RestClient restClient;
    private final ExternalApiProperties properties;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public TatoebaService(RestClient externalRestClient, ExternalApiProperties properties) {
        this.restClient = externalRestClient;
        this.properties = properties;
    }

    public List<ExampleSentenceDto> findExamples(String query) {
        ExternalApiProperties.Tatoeba config = properties.getTatoeba();
        if (!config.isEnabled() || query == null || query.isBlank()) {
            return List.of();
        }

        String key = query.trim().toLowerCase(Locale.ROOT);
        CacheEntry cached = cache.get(key);
        if (cached != null && cached.isFresh()) {
            return cached.sentences();
        }

        try {
            String url = UriComponentsBuilder.fromHttpUrl(config.getBaseUrl() + "/search")
                    .queryParam("from", "deu")
                    .queryParam("to", "eng")
                    .queryParam("query", key)
                    .queryParam("sort", "relevance")
                    .build()
                    .toUriString();

            JsonNode response = restClient.get().uri(url).retrieve().body(JsonNode.class);
            List<ExampleSentenceDto> sentences = extract(response, key, config);
            cache.put(key, new CacheEntry(sentences, Instant.now()));
            return sentences;
        } catch (Exception e) {
            // An enrichment feature must never break a lesson
            log.warn("Tatoeba lookup for '{}' failed: {}", key, e.toString());
            cache.put(key, new CacheEntry(List.of(), Instant.now()));
            return List.of();
        }
    }

    private List<ExampleSentenceDto> extract(JsonNode response, String query,
            ExternalApiProperties.Tatoeba config) {
        List<ExampleSentenceDto> sentences = new ArrayList<>();
        if (response == null || !response.hasNonNull("results")) {
            return sentences;
        }

        for (JsonNode result : response.get("results")) {
            if (sentences.size() >= config.getMaxResults()) {
                break;
            }

            String german = result.path("text").asText(null);
            if (!isUsable(german, query, config)) {
                continue;
            }

            String english = firstEnglishTranslation(result);
            if (english == null || !isClean(english)) {
                continue;
            }

            sentences.add(new ExampleSentenceDto(german.trim(), english.trim(), "Tatoeba"));
        }

        return sentences;
    }

    private String firstEnglishTranslation(JsonNode result) {
        // translations is an array of arrays: [[direct...], [indirect...]]
        for (JsonNode group : result.path("translations")) {
            for (JsonNode translation : group) {
                if ("eng".equals(translation.path("lang").asText())) {
                    String text = translation.path("text").asText(null);
                    if (text != null && !text.isBlank()) {
                        return text;
                    }
                }
            }
        }
        return null;
    }

    private boolean isUsable(String german, String query, ExternalApiProperties.Tatoeba config) {
        if (german == null || german.isBlank()) {
            return false;
        }
        int length = german.trim().length();
        if (length < config.getMinLength() || length > config.getMaxLength()) {
            return false;
        }
        if (!isClean(german)) {
            return false;
        }
        // The sentence has to actually show the word the lesson is about
        return german.toLowerCase(Locale.ROOT).contains(query);
    }

    /** Rejects markup, urls and other things that read badly in a lesson. */
    private boolean isClean(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("http") || lower.contains("www.") || lower.contains("@")) {
            return false;
        }
        return !text.matches(".*[<>\\[\\]{}|\\\\].*");
    }

    private record CacheEntry(List<ExampleSentenceDto> sentences, Instant fetchedAt) {
        boolean isFresh() {
            return Duration.between(fetchedAt, Instant.now()).compareTo(CACHE_TTL) < 0;
        }
    }
}
