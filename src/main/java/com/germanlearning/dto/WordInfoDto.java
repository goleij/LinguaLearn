package com.germanlearning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * What the Word Explorer shows for a German word.
 *
 * Wiktionary entries are written by hand and vary a lot, so every field except
 * the word itself may be missing; {@code found} tells the UI whether there was
 * an entry at all.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WordInfoDto(
        String word,
        boolean found,
        String wordType,
        String article,
        List<String> meanings,
        List<String> examples,
        List<String> synonyms,
        String origin,
        String source,
        String sourceUrl) {

    public static WordInfoDto notFound(String word, String sourceUrl) {
        return new WordInfoDto(word, false, null, null, List.of(), List.of(), List.of(),
                null, "German Wiktionary", sourceUrl);
    }
}
