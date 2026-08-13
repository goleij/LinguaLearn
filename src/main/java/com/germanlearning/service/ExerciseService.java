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

    /**
     * Checks an answer. Deliberately says nothing about XP: scoring lives in
     * {@link ScoreService} and is persisted by
     * {@link ProgressService#submitAnswer(Long, Long, Long, String)}.
     */
    public ExerciseResult validateAnswer(Long exerciseId, String userAnswer) {
        Optional<Exercise> exerciseOpt = exerciseRepository.findById(exerciseId);
        if (exerciseOpt.isEmpty()) {
            throw new IllegalArgumentException("Exercise not found");
        }

        return validateAnswer(exerciseOpt.get(), userAnswer);
    }

    public ExerciseResult validateAnswer(Exercise exercise, String userAnswer) {
        boolean isCorrect = exercise.validateAnswer(userAnswer);

        return new ExerciseResult(
                isCorrect,
                exercise.getCorrectAnswer(),
                exercise.getExplanation());
    }

    public static class ExerciseResult {
        private final boolean correct;
        private final String correctAnswer;
        private final String explanation;

        public ExerciseResult(boolean correct, String correctAnswer, String explanation) {
            this.correct = correct;
            this.correctAnswer = correctAnswer;
            this.explanation = explanation;
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
    }
}
