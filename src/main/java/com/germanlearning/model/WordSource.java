package com.germanlearning.model;

/** Where a word in the word bank came from. */
public enum WordSource {

    /** Added by the learner from the Word Explorer. */
    WORD_EXPLORER("Looked up"),

    /** Added by the learner from a lesson's vocabulary list. */
    VOCABULARY("From a lesson"),

    /** Added on its own after the learner got it wrong in a lesson. */
    MISTAKE("Missed in a lesson"),

    MANUAL("Added by hand");

    private final String label;

    WordSource(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
