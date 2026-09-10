package com.germanlearning.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One word in a learner's personal word bank, together with its review state.
 *
 * Scheduling is a Leitner box system: a word starts in box 0 and moves up one
 * box each time it is remembered, waiting longer before it comes back. Getting
 * it wrong drops it to box 0, so it returns immediately. That keeps the whole
 * schedule in two columns — {@code box} and {@code dueAt} — and needs no
 * background job: a word is due when its date has passed.
 */
@Entity
@Table(
        name = "saved_words",
        uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "german" }))
public class SavedWord {

    /** How long each box waits before the word comes back, in days. */
    private static final int[] INTERVAL_DAYS = { 0, 1, 3, 7, 16, 35 };

    public static final int LAST_BOX = INTERVAL_DAYS.length - 1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String german;

    @Column(nullable = false)
    private String english;

    /** Where the word came in from, so the list can explain itself. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "varchar(255) not null default 'MANUAL'")
    private WordSource source = WordSource.MANUAL;

    private String topic;

    @Enumerated(EnumType.STRING)
    @Column(name = "cefr_level")
    private CefrLevel cefrLevel;

    /** The lesson the word was picked up in, for context only. */
    @Column(name = "lesson_name")
    private String lessonName;

    @Column(name = "box", nullable = false, columnDefinition = "integer not null default 0")
    private int box;

    @Column(name = "due_at")
    private LocalDateTime dueAt;

    @Column(name = "review_count", nullable = false, columnDefinition = "integer not null default 0")
    private int reviewCount;

    @Column(name = "correct_count", nullable = false, columnDefinition = "integer not null default 0")
    private int correctCount;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "added_at", nullable = false)
    private LocalDateTime addedAt = LocalDateTime.now();

    protected SavedWord() {
    }

    public SavedWord(User user, String german, String english, WordSource source) {
        this.user = user;
        this.german = german;
        this.english = english;
        this.source = source == null ? WordSource.MANUAL : source;
        this.addedAt = LocalDateTime.now();
        // A brand new word is due straight away
        this.dueAt = LocalDateTime.now();
    }

    /**
     * Records a review. Remembering moves the word up a box; forgetting sends
     * it back to the start so it is asked again in this same session.
     */
    public void review(boolean remembered) {
        reviewCount++;
        if (remembered) {
            correctCount++;
            box = Math.min(box + 1, LAST_BOX);
        } else {
            box = 0;
        }
        lastReviewedAt = LocalDateTime.now();
        dueAt = LocalDateTime.now().plusDays(INTERVAL_DAYS[box]);
    }

    public boolean isDue() {
        return dueAt == null || !dueAt.isAfter(LocalDateTime.now());
    }

    /** A word is treated as learned once it has survived to the last box. */
    public boolean isLearned() {
        return box >= LAST_BOX;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public String getGerman() {
        return german;
    }

    public void setGerman(String german) {
        this.german = german;
    }

    public String getEnglish() {
        return english;
    }

    public void setEnglish(String english) {
        this.english = english;
    }

    public WordSource getSource() {
        return source;
    }

    public void setSource(WordSource source) {
        this.source = source;
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public CefrLevel getCefrLevel() {
        return cefrLevel;
    }

    public void setCefrLevel(CefrLevel cefrLevel) {
        this.cefrLevel = cefrLevel;
    }

    public String getLessonName() {
        return lessonName;
    }

    public void setLessonName(String lessonName) {
        this.lessonName = lessonName;
    }

    public int getBox() {
        return box;
    }

    public void setBox(int box) {
        this.box = box;
    }

    public LocalDateTime getDueAt() {
        return dueAt;
    }

    public void setDueAt(LocalDateTime dueAt) {
        this.dueAt = dueAt;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public LocalDateTime getLastReviewedAt() {
        return lastReviewedAt;
    }

    public LocalDateTime getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(LocalDateTime addedAt) {
        this.addedAt = addedAt;
    }
}
