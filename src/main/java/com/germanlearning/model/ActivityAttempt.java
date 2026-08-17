package com.germanlearning.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * One graded answer, kept as history.
 *
 * Nothing reads this yet. It exists so mistake tracking, adaptive practice and
 * Smart Review can be built on real data later: every row carries the skill,
 * topic and difficulty of the activity as it was answered, so those features
 * will not need to join back through content that may have changed.
 */
@Entity
@Table(name = "activity_attempts")
public class ActivityAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "activity_id", nullable = false)
    private LessonActivity activity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityType activityType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivityPhase phase;

    @Enumerated(EnumType.STRING)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    private Difficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(name = "cefr_level")
    private CefrLevel cefrLevel;

    private String topic;

    @Column(length = 1000)
    private String answer;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int xpAwarded;

    @Column(nullable = false)
    private LocalDateTime answeredAt = LocalDateTime.now();

    public ActivityAttempt() {
    }

    public static ActivityAttempt of(User user, LessonActivity activity, String answer,
            boolean correct, int xpAwarded) {
        ActivityAttempt attempt = new ActivityAttempt();
        attempt.user = user;
        attempt.lesson = activity.getLesson();
        attempt.activity = activity;
        attempt.activityType = activity.getType();
        attempt.phase = activity.getPhase();
        attempt.skill = activity.getSkill();
        attempt.difficulty = activity.getDifficulty();
        attempt.cefrLevel = activity.getCefrLevel();
        attempt.topic = activity.getTopic();
        attempt.answer = answer;
        attempt.correct = correct;
        attempt.xpAwarded = xpAwarded;
        attempt.answeredAt = LocalDateTime.now();
        return attempt;
    }

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public LessonActivity getActivity() {
        return activity;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public ActivityPhase getPhase() {
        return phase;
    }

    public Skill getSkill() {
        return skill;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public CefrLevel getCefrLevel() {
        return cefrLevel;
    }

    public String getTopic() {
        return topic;
    }

    public String getAnswer() {
        return answer;
    }

    public boolean isCorrect() {
        return correct;
    }

    public int getXpAwarded() {
        return xpAwarded;
    }

    public LocalDateTime getAnsweredAt() {
        return answeredAt;
    }
}
