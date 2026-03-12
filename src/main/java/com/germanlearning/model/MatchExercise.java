package com.germanlearning.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import java.util.HashMap;
import java.util.Map;

@Entity
@DiscriminatorValue("MATCH_PAIRS")
public class MatchExercise extends Exercise {

    @Column(length = 2000)
    private String pairsData;

    public MatchExercise() {
        setExerciseType(ExerciseType.MATCH_PAIRS);
    }

    public MatchExercise(String question, String correctAnswer, String explanation,
                          Map<String, String> pairs) {
        super(question, correctAnswer, explanation, ExerciseType.MATCH_PAIRS);
        setPairs(pairs);
    }

    @Override
    public boolean validateAnswer(String userAnswer) {
        if (userAnswer == null || getCorrectAnswer() == null) {
            return false;
        }
        
        Map<String, String> correctPairs = getPairs();
        String[] userPairsArray = userAnswer.split(";");
        
        int correctCount = 0;
        for (String userPair : userPairsArray) {
            String[] parts = userPair.split(":");
            if (parts.length == 2) {
                String key = parts[0].trim().toLowerCase();
                String value = parts[1].trim().toLowerCase();
                
                for (Map.Entry<String, String> entry : correctPairs.entrySet()) {
                    if (entry.getKey().toLowerCase().equals(key) &&
                        entry.getValue().toLowerCase().equals(value)) {
                        correctCount++;
                        break;
                    }
                }
            }
        }
        
        return correctCount == correctPairs.size();
    }

    public Map<String, String> getPairs() {
        Map<String, String> result = new HashMap<>();
        if (pairsData == null || pairsData.isEmpty()) {
            return result;
        }
        
        String[] pairs = pairsData.split(";");
        for (String pair : pairs) {
            String[] parts = pair.split(":");
            if (parts.length == 2) {
                result.put(parts[0].trim(), parts[1].trim());
            }
        }
        return result;
    }

    public void setPairs(Map<String, String> pairs) {
        if (pairs == null || pairs.isEmpty()) {
            this.pairsData = "";
            return;
        }
        
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : pairs.entrySet()) {
            if (sb.length() > 0) {
                sb.append(";");
            }
            sb.append(entry.getKey()).append(":").append(entry.getValue());
        }
        this.pairsData = sb.toString();
    }

    public String getPairsData() {
        return pairsData;
    }

    public void setPairsData(String pairsData) {
        this.pairsData = pairsData;
    }
}
