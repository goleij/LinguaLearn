package com.germanlearning.model;

import jakarta.persistence.*;

/**
 * One step inside a lesson: a teaching card, an interactive exercise or a
 * checkpoint banner.
 *
 * Everything that differs per type (options, word bank, pairs, dialogue lines,
 * audio text, ...) lives in {@link #payload} as JSON, so a new activity type
 * needs a grader and a renderer but no schema change. The shared metadata
 * (skill, difficulty, topic) is stored in columns because mistake tracking and
 * adaptive practice will query on it.
 */
@Entity
@Table(name = "lesson_activities")
public class LessonActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityPhase phase;

    /** Order inside the lesson. */
    @Column(nullable = false)
    private int position;

    /** What the learner is asked to do, e.g. "Choose the correct answer". */
    @Column(length = 500)
    private String instruction;

    /** The question, sentence or heading. */
    @Column(length = 1000)
    private String prompt;

    /** Supporting text: the situation, a dialogue intro, a learn card body. */
    @Column(length = 2000)
    private String context;

    @Column(length = 500)
    private String hint;

    @Column(length = 1000)
    private String explanation;

    @Enumerated(EnumType.STRING)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    /** The CEFR level this activity is written for. */
    @Enumerated(EnumType.STRING)
    @Column(name = "cefr_level")
    private CefrLevel cefrLevel;

    private String topic;

    @Column(nullable = false)
    private int xpReward = 10;

    /** Canonical answer; alternatives separated by "|". Null when not graded. */
    @Column(length = 500)
    private String correctAnswer;

    /** Type specific JSON configuration. */
    @Column(length = 4000)
    private String payload;

    public LessonActivity() {
    }

    /**
     * Whether this activity expects an answer.
     *
     * Type decides it, with one exception: a dialogue is teaching content when
     * it only shows a conversation, and graded when it asks the learner to
     * pick the reply.
     */
    public boolean isGraded() {
        if (type == null) {
            return false;
        }
        if (type.isGraded()) {
            return true;
        }
        return type == ActivityType.DIALOGUE && correctAnswer != null;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public ActivityPhase getPhase() {
        return phase;
    }

    public void setPhase(ActivityPhase phase) {
        this.phase = phase;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public String getInstruction() {
        return instruction;
    }

    public void setInstruction(String instruction) {
        this.instruction = instruction;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public CefrLevel getCefrLevel() {
        return cefrLevel;
    }

    public void setCefrLevel(CefrLevel cefrLevel) {
        this.cefrLevel = cefrLevel;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getPayload() {
        return payload;
    }

    public void setPayload(String payload) {
        this.payload = payload;
    }
}
