package com.germanlearning.model;

/**
 * The stage of the lesson an activity belongs to.
 *
 * A learner walks them in order: read the material (LEARN), see it used
 * (CONTEXT), try it with support (GUIDED_PRACTICE), work without support
 * (PRACTICE), produce something of their own (APPLY) and finally prove it
 * (CHECKPOINT). Only the checkpoint decides whether the lesson is completed,
 * so mistakes anywhere else are part of learning rather than failure.
 */
public enum ActivityPhase {

    LEARN("Learn", false),
    CONTEXT("Context", false),
    GUIDED_PRACTICE("Guided practice", true),
    PRACTICE("Practice", true),
    APPLY("Apply", true),
    CHECKPOINT("Checkpoint", true);

    private final String label;
    private final String scored;

    ActivityPhase(String label, boolean scored) {
        this.label = label;
        this.scored = String.valueOf(scored);
    }

    public String getLabel() {
        return label;
    }

    /** Whether answers in this phase count towards the lesson score. */
    public boolean isScored() {
        return Boolean.parseBoolean(scored);
    }

    /** Only the checkpoint decides completion and unlocking. */
    public boolean decidesCompletion() {
        return this == CHECKPOINT;
    }

    /** Guided practice offers the hint up front. */
    public boolean offersHintUpFront() {
        return this == GUIDED_PRACTICE;
    }
}
