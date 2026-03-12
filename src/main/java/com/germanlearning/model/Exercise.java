package com.germanlearning.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "exercises")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
// این ستون برای تشخیص زیرکلاس‌ها (مثل MultipleChoice یا FillInBlanks) استفاده
// می‌شود
@DiscriminatorColumn(name = "discriminator_type", discriminatorType = DiscriminatorType.STRING)
public abstract class Exercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 1000)
    private String question;

    @NotBlank
    @Column(nullable = false, length = 500)
    private String correctAnswer;

    @Column(length = 1000)
    private String explanation;

    @Column(nullable = false)
    private int xpReward = 10;

    @Enumerated(EnumType.STRING)
    @Column(name = "exercise_type_enum", nullable = false)
    // تغییر نام ستون بالا برای جلوگیری از تداخل با DiscriminatorColumn
    private ExerciseType exerciseType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    public Exercise() {
    }

    public Exercise(String question, String correctAnswer, String explanation, ExerciseType exerciseType) {
        this.question = question;
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
        this.exerciseType = exerciseType;
    }

    public abstract boolean validateAnswer(String userAnswer);

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public int getXpReward() {
        return xpReward;
    }

    public void setXpReward(int xpReward) {
        this.xpReward = xpReward;
    }

    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public void setLesson(Lesson lesson) {
        this.lesson = lesson;
    }
}