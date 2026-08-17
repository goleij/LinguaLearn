package com.germanlearning.model;

/**
 * How demanding an activity is. The numeric level exists so future adaptive
 * practice can compare and order activities without switch statements.
 */
public enum Difficulty {

    EASY(1),
    MEDIUM(2),
    HARD(3);

    private final int level;

    Difficulty(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
