package com.germanlearning.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("FILL_BLANK")
public class FillBlankExercise extends Exercise {

    @Column(length = 1000)
    private String sentenceTemplate;

    public FillBlankExercise() {
        setExerciseType(ExerciseType.FILL_BLANK);
    }

    public FillBlankExercise(String question, String correctAnswer, String explanation,
                              String sentenceTemplate) {
        super(question, correctAnswer, explanation, ExerciseType.FILL_BLANK);
        this.sentenceTemplate = sentenceTemplate;
    }

    @Override
    public boolean validateAnswer(String userAnswer) {
        if (userAnswer == null || getCorrectAnswer() == null) {
            return false;
        }
        String normalizedUser = userAnswer.trim().toLowerCase();
        String normalizedCorrect = getCorrectAnswer().trim().toLowerCase();
        
        if (normalizedUser.equals(normalizedCorrect)) {
            return true;
        }
        
        String[] acceptableAnswers = getCorrectAnswer().split("\\|");
        for (String acceptable : acceptableAnswers) {
            if (normalizedUser.equals(acceptable.trim().toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    public String getSentenceTemplate() {
        return sentenceTemplate;
    }

    public void setSentenceTemplate(String sentenceTemplate) {
        this.sentenceTemplate = sentenceTemplate;
    }

    public String getSentenceWithBlank() {
        if (sentenceTemplate == null) {
            return getQuestion();
        }
        return sentenceTemplate;
    }
}
