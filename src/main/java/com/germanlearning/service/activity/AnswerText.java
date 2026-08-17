package com.germanlearning.service.activity;

import java.util.Arrays;
import java.util.Locale;

/**
 * Text comparison shared by the graders.
 *
 * Answers are compared case insensitively, without surrounding punctuation and
 * with German umlauts folded to their typed equivalents, so "heisse" is
 * accepted for "heiße" by learners without a German keyboard.
 */
public final class AnswerText {

    private AnswerText() {
    }

    public static String normalize(String text) {
        if (text == null) {
            return "";
        }
        String normalized = text.trim().toLowerCase(Locale.ROOT);
        normalized = normalized
                .replace("ä", "ae")
                .replace("ö", "oe")
                .replace("ü", "ue")
                .replace("ß", "ss");
        normalized = normalized.replaceAll("[.,!?;:\"'„“”()]", "");
        normalized = normalized.replaceAll("\\s+", " ");
        return normalized.trim();
    }

    /**
     * True when the answer matches the expected value, where the expected value
     * may list alternatives separated by "|".
     */
    public static boolean matches(String answer, String expected) {
        if (expected == null) {
            return false;
        }
        String normalizedAnswer = normalize(answer);
        if (normalizedAnswer.isEmpty()) {
            return false;
        }
        return Arrays.stream(expected.split("\\|"))
                .map(AnswerText::normalize)
                .anyMatch(normalizedAnswer::equals);
    }

    /** The first alternative, which is what gets shown as "the" correct answer. */
    public static String primary(String expected) {
        if (expected == null) {
            return null;
        }
        int separator = expected.indexOf('|');
        return separator < 0 ? expected : expected.substring(0, separator);
    }

    /**
     * True when the answer is only a typo away from the expected value, so the
     * learner can be told they were close rather than simply wrong.
     */
    public static boolean isNearMiss(String answer, String expected) {
        String normalizedAnswer = normalize(answer);
        if (normalizedAnswer.isEmpty() || expected == null) {
            return false;
        }
        return Arrays.stream(expected.split("\\|"))
                .map(AnswerText::normalize)
                .anyMatch(candidate -> {
                    int allowed = candidate.length() <= 4 ? 1 : 2;
                    return editDistance(normalizedAnswer, candidate) <= allowed;
                });
    }

    /** Standard Levenshtein distance, iterative and allocation friendly. */
    static int editDistance(String left, String right) {
        int[] previous = new int[right.length() + 1];
        int[] current = new int[right.length() + 1];

        for (int j = 0; j <= right.length(); j++) {
            previous[j] = j;
        }

        for (int i = 1; i <= left.length(); i++) {
            current[0] = i;
            for (int j = 1; j <= right.length(); j++) {
                int substitution = previous[j - 1] + (left.charAt(i - 1) == right.charAt(j - 1) ? 0 : 1);
                current[j] = Math.min(Math.min(current[j - 1] + 1, previous[j] + 1), substitution);
            }
            int[] swap = previous;
            previous = current;
            current = swap;
        }

        return previous[right.length()];
    }

    /** Words of an answer, for order and content comparisons. */
    public static String[] words(String text) {
        String normalized = normalize(text);
        return normalized.isEmpty() ? new String[0] : normalized.split(" ");
    }
}
