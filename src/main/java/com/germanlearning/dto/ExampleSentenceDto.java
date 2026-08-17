package com.germanlearning.dto;

/**
 * A real German sentence with its English translation.
 *
 * @param source where it came from, e.g. "Tatoeba", so the UI can credit it
 */
public record ExampleSentenceDto(String german, String english, String source) {
}
