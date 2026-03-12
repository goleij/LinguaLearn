package com.germanlearning.service;

import com.germanlearning.model.Exercise;
import com.germanlearning.repository.ExerciseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class ExerciseService {

    private final ExerciseRepository exerciseRepository;

    public ExerciseService(ExerciseRepository exerciseRepository) {
        this.exerciseRepository = exerciseRepository;
    }

    public Optional<Exercise> getExerciseById(Long exerciseId) {
        return exerciseRepository.findById(exerciseId);
    }

    public List<Exercise> getExercisesForLesson(Long lessonId) {
        return exerciseRepository.findByLessonIdOrderByIdAsc(lessonId);
    }

    public int getExerciseCount(Long lessonId) {
        return exerciseRepository.countByLessonId(lessonId);
    }

    public ExerciseResult validateAnswer(Long exerciseId, String userAnswer) {
        Optional<Exercise> exerciseOpt = exerciseRepository.findById(exerciseId);
        if (exerciseOpt.isEmpty()) {
            throw new IllegalArgumentException("Exercise not found");
        }

        Exercise exercise = exerciseOpt.get();
        boolean isCorrect = exercise.validateAnswer(userAnswer);

        return new ExerciseResult(
                isCorrect,
                exercise.getCorrectAnswer(),
                exercise.getExplanation(),
                isCorrect ? exercise.getXpReward() : 0
        );
    }

    public static class ExerciseResult {
        private final boolean correct;
        private final String correctAnswer;
        private final String explanation;
        private final int xpEarned;

        public ExerciseResult(boolean correct, String correctAnswer, String explanation, int xpEarned) {
            this.correct = correct;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
            this.xpEarned = xpEarned;
        }

        public boolean isCorrect() {
            return correct;
        }

        public String getCorrectAnswer() {
            return correctAnswer;
        }

        public String getExplanation() {
            return explanation;
        }

        public int getXpEarned() {
            return xpEarned;
        }
    }
}
