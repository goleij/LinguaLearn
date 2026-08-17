package com.germanlearning.model;

/**
 * The competence an activity trains. Stored on every answer so that mistake
 * tracking and adaptive practice can group by skill later.
 */
public enum Skill {
    VOCABULARY,
    GRAMMAR,
    WORD_ORDER,
    LISTENING,
    WRITING,
    READING,
    ARTICLES,
    PREPOSITIONS,
    VERB_CONJUGATION
}
