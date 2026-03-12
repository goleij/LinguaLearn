package com.germanlearning.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "progress")
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private int correctAnswers = 0;

    @Column(nullable = false)
    private int totalAnswers = 0;

    @Column(nullable = false)
    private int attempts = 0;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(nullable = false)
    private int xpEarned = 0;

    @Column(nullable = false)
    private boolean xpLocked = false;

    @Column(nullable = false)
    private double bestScore = 0.0;

    private LocalDateTime completedAt;

    private LocalDateTime lastAttemptAt;

    public Progress() {
    }

    public Progress(User user, Lesson lesson) {
        this.user = user;
        this.lesson = lesson;
        this.lastAttemptAt = LocalDateTime.now();
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

    public void setUser(User user) {
        this.user = user;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }

    public int getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(int correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public void incrementCorrectAnswers() {
        this.correctAnswers++;
    }

    public int getTotalAnswers() {
        return totalAnswers;
    }

    public void setTotalAnswers(int totalAnswers) {
        this.totalAnswers = totalAnswers;
    }

    public void incrementTotalAnswers() {
        this.totalAnswers++;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public void incrementAttempts() {
        this.attempts++;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
        if (completed && this.completedAt == null) {
            this.completedAt = LocalDateTime.now();
        }
    }

    public int getXpEarned() {
        return xpEarned;
    }

    public void setXpEarned(int xpEarned) {
        this.xpEarned = xpEarned;
    }

    public void addXp(int xp) {
        this.xpEarned += xp;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public LocalDateTime getLastAttemptAt() {
        return lastAttemptAt;
    }

    public void setLastAttemptAt(LocalDateTime lastAttemptAt) {
        this.lastAttemptAt = lastAttemptAt;
    }

    public double getScorePercentage() {
        if (totalAnswers == 0) {
            return 0.0;
        }
        return (double) correctAnswers / totalAnswers * 100.0;
    }

    public boolean hasPassedThreshold() {
        return getScorePercentage() >= 80.0;
    }

    public boolean isXpLocked() {
        return xpLocked;
    }

    public void setXpLocked(boolean xpLocked) {
        this.xpLocked = xpLocked;
    }

    public double getBestScore() {
        return bestScore;
    }

    public void setBestScore(double bestScore) {
        this.bestScore = bestScore;
    }

    public void updateBestScore() {
        double currentScore = getScorePercentage();
        if (currentScore > this.bestScore) {
            this.bestScore = currentScore;
        }
    }
}
