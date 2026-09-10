package com.germanlearning.dto;

/** Adding a word to the bank by hand, from the Word Explorer or a lesson. */
public record SaveWordRequest(String german, String english, String source, String topic,
        String lessonName) {
}
