package com.germanlearning.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("MULTIPLE_CHOICE")
public class MultipleChoiceExercise extends Exercise {

    @Column(length = 1000)
    private String options;

    @Column
    private int correctOptionIndex;

    public MultipleChoiceExercise() {
        setExerciseType(ExerciseType.MULTIPLE_CHOICE);
    }

    public MultipleChoiceExercise(String question, String correctAnswer, String explanation,
                                   String options, int correctOptionIndex) {
        super(question, correctAnswer, explanation, ExerciseType.MULTIPLE_CHOICE);
        this.options = options;
        this.correctOptionIndex = correctOptionIndex;
    }

    @Override
    public boolean validateAnswer(String userAnswer) {
        if (userAnswer == null) {
            return false;
        }
        try {
            int selectedIndex = Integer.parseInt(userAnswer.trim());
            return selectedIndex == correctOptionIndex;
        } catch (NumberFormatException e) {
            return userAnswer.trim().equalsIgnoreCase(getCorrectAnswer().trim());
        }
    }

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String[] getOptionsArray() {
        if (options == null || options.isEmpty()) {
            return new String[0];
        }
        return options.split("\\|");
    }

    public void setOptionsFromArray(String[] optionsArray) {
        this.options = String.join("|", optionsArray);
    }

    public int getCorrectOptionIndex() {
        return correctOptionIndex;
    }

    public void setCorrectOptionIndex(int correctOptionIndex) {
        this.correctOptionIndex = correctOptionIndex;
    }
}
