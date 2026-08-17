package com.germanlearning.model;

/**
 * Common European Framework level.
 *
 * Courses, lessons and individual activities carry one, so content can be
 * filtered by level and so later features (Smart Review, adaptive practice)
 * can tell an A1 mistake from a B2 one.
 */
public enum CefrLevel {

    A1("Beginner", "Everyday phrases and very basic sentences"),
    A2("Elementary", "Simple exchanges about familiar routine matters"),
    B1("Intermediate", "Connected text on familiar topics, opinions and plans"),
    B2("Upper intermediate", "Complex text, abstract topics and detailed argument");

    private final String label;
    private final String description;

    CefrLevel(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    /** Ordering helper: A1 is the lowest. */
    public boolean isAtLeast(CefrLevel other) {
        return this.ordinal() >= other.ordinal();
    }
}
