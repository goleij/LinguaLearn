package com.germanlearning.dto;

import java.util.List;

/**
 * The writing coach's answer.
 *
 * @param available false when the checker could not be reached, so the UI can
 *                  say so instead of pretending the text is perfect
 */
public record GrammarFeedbackDto(
        boolean available,
        String text,
        int issueCount,
        List<GrammarIssueDto> issues,
        String message) {

    public record GrammarIssueDto(
            String message,
            String shortMessage,
            String category,
            /** The exact part of the learner's text the issue refers to. */
            String excerpt,
            int offset,
            int length,
            List<String> suggestions) {
    }

    public static GrammarFeedbackDto unavailable(String text, String message) {
        return new GrammarFeedbackDto(false, text, 0, List.of(), message);
    }
}
