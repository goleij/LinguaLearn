package com.germanlearning.model;

import java.util.Set;

/**
 * What a lesson activity is.
 *
 * The teaching types carry no answer; the rest are graded by a matching
 * {@link com.germanlearning.service.activity.ActivityGrader}. Adding a type
 * means adding a grader (if it is graded) and a renderer in the frontend.
 */
public enum ActivityType {

    // Teaching content
    LEARN_CARD,
    VOCABULARY,
    GRAMMAR_TIP,
    DIALOGUE,
    REAL_EXAMPLE,
    CHECKPOINT,

    // Interactive
    MULTIPLE_CHOICE,
    FILL_BLANK,
    SENTENCE_BUILDER,
    MATCH_PAIRS,
    TRANSLATION,
    CONTEXT_CHOICE,
    SHORT_WRITING,
    LISTENING;

    private static final Set<ActivityType> TEACHING = Set.of(
            LEARN_CARD, VOCABULARY, GRAMMAR_TIP, DIALOGUE, REAL_EXAMPLE, CHECKPOINT);

    /** True when the activity expects an answer that can be graded. */
    public boolean isGraded() {
        return !TEACHING.contains(this);
    }
}
