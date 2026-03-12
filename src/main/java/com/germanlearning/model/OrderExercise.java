package com.germanlearning.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.Arrays;
import java.util.List;

@Entity
@DiscriminatorValue("SENTENCE_ORDER")
public class OrderExercise extends Exercise {

    @Column(length = 1000)
    private String shuffledWords;

    @Column(length = 1000)
    private String correctOrder;

    public OrderExercise() {
        setExerciseType(ExerciseType.SENTENCE_ORDER);
    }

    public OrderExercise(String question, String correctAnswer, String explanation,
                          String shuffledWords, String correctOrder) {
        super(question, correctAnswer, explanation, ExerciseType.SENTENCE_ORDER);
        this.shuffledWords = shuffledWords;
        this.correctOrder = correctOrder;
    }

    @Override
    public boolean validateAnswer(String userAnswer) {
        if (userAnswer == null || correctOrder == null) {
            return false;
        }
        
        String normalizedUser = userAnswer.trim().toLowerCase().replaceAll("\\s+", " ");
        String normalizedCorrect = correctOrder.trim().toLowerCase().replaceAll("\\s+", " ");
        
        return normalizedUser.equals(normalizedCorrect);
    }

    public String getShuffledWords() {
        return shuffledWords;
    }

    public void setShuffledWords(String shuffledWords) {
        this.shuffledWords = shuffledWords;
    }

    public List<String> getShuffledWordsList() {
        if (shuffledWords == null || shuffledWords.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(shuffledWords.split("\\|"));
    }

    public void setShuffledWordsFromList(List<String> words) {
        this.shuffledWords = String.join("|", words);
    }

    public String getCorrectOrder() {
        return correctOrder;
    }

    public void setCorrectOrder(String correctOrder) {
        this.correctOrder = correctOrder;
    }
}
