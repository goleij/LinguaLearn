package com.germanlearning.service;

import com.germanlearning.model.Exercise;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.MultipleChoiceExercise;
import com.germanlearning.model.Progress;
import com.germanlearning.model.User;
import com.germanlearning.repository.ExerciseRepository;
import com.germanlearning.repository.LessonRepository;
import com.germanlearning.repository.ProgressRepository;
import com.germanlearning.repository.UserRepository;
import com.germanlearning.service.ProgressService.AnswerResult;
import com.germanlearning.service.ProgressService.LessonCompletionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Covers the XP rules of the single answer entry point: one formula, a real
 * streak, no double payouts on retry, and UI numbers equal to stored numbers.
 */
class ProgressServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long LESSON_ID = 100L;

    private ProgressRepository progressRepository;
    private UserRepository userRepository;
    private ProgressService progressService;

    private User user;
    private Lesson lesson;
    private final Map<Long, Exercise> exercises = new HashMap<>();
    private Progress storedProgress;

    @BeforeEach
    void setUp() {
        progressRepository = mock(ProgressRepository.class);
        userRepository = mock(UserRepository.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        ExerciseRepository exerciseRepository = mock(ExerciseRepository.class);

        user = new User("anna", "anna@example.com", "hash");
        user.setId(USER_ID);

        lesson = new Lesson("Greetings", 0, "Basic greetings");
        lesson.setId(LESSON_ID);

        exercises.clear();
        storedProgress = null;

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(exerciseRepository.findById(anyLong()))
                .thenAnswer(inv -> Optional.ofNullable(exercises.get(inv.<Long>getArgument(0))));

        // A tiny in-memory stand-in for the progress table
        when(progressRepository.findByUserIdAndLessonId(USER_ID, LESSON_ID))
                .thenAnswer(inv -> Optional.ofNullable(storedProgress));
        when(progressRepository.save(any(Progress.class))).thenAnswer(inv -> {
            storedProgress = inv.getArgument(0);
            return storedProgress;
        });
        when(progressRepository.saveAndFlush(any(Progress.class))).thenAnswer(inv -> {
            storedProgress = inv.getArgument(0);
            return storedProgress;
        });

        progressService = new ProgressService(progressRepository, userRepository, lessonRepository,
                exerciseRepository, new ExerciseService(exerciseRepository), new ScoreService());
    }

    private Exercise exercise(long id, int xpReward) {
        MultipleChoiceExercise exercise = new MultipleChoiceExercise(
                "Hallo?", "Hello", "greeting", "Hello|Bye", 0);
        exercise.setId(id);
        exercise.setXpReward(xpReward);
        exercise.setLesson(lesson);
        exercises.put(id, exercise);
        return exercise;
    }

    private AnswerResult answer(long exerciseId, boolean correct) {
        return progressService.submitAnswer(USER_ID, LESSON_ID, exerciseId, correct ? "0" : "1");
    }

    @Test
    void awardsExerciseRewardAndPersistsExactlyWhatItReturns() {
        exercise(1L, 10);

        AnswerResult result = answer(1L, true);

        assertTrue(result.isCorrect());
        assertEquals(10, result.getXpAwarded());
        assertEquals(result.getXpAwarded(), storedProgress.getXpEarned());
        assertEquals(result.getLessonXpEarned(), storedProgress.getXpEarned());
        assertEquals(result.getUserTotalXp(), user.getTotalXp());
        assertEquals(10, user.getTotalXp());
    }

    @Test
    void wrongAnswerEarnsNothingAndResetsTheStreak() {
        exercise(1L, 10);
        exercise(2L, 10);
        exercise(3L, 10);

        answer(1L, true);
        answer(2L, true);
        AnswerResult wrong = answer(3L, false);

        assertFalse(wrong.isCorrect());
        assertEquals(0, wrong.getXpAwarded());
        assertEquals(0, wrong.getCurrentStreak());
        assertEquals(20, storedProgress.getXpEarned());
        assertEquals(2, storedProgress.getCorrectAnswers());
        assertEquals(3, storedProgress.getTotalAnswers());
    }

    @Test
    void streakBonusFollowsConsecutiveCorrectAnswersNotTheCorrectCount() {
        for (long id = 1; id <= 5; id++) {
            exercise(id, 10);
        }

        answer(1L, true); // streak 1 -> 10
        answer(2L, true); // streak 2 -> 10
        answer(3L, false); // streak reset
        answer(4L, true); // streak 1 -> 10, not a bonus even though 3 answers are correct
        AnswerResult fifth = answer(5L, true); // streak 2 -> 10

        assertEquals(10, fifth.getXpAwarded());
        assertEquals(2, fifth.getCurrentStreak());
        assertEquals(40, storedProgress.getXpEarned());

        // Three in a row does earn the bonus
        exercise(6L, 10);
        AnswerResult sixth = answer(6L, true);
        assertEquals(3, sixth.getCurrentStreak());
        assertEquals(15, sixth.getXpAwarded());
    }

    @Test
    void answeringTheSameExerciseAgainPaysNothing() {
        exercise(1L, 10);

        assertEquals(10, answer(1L, true).getXpAwarded());
        assertEquals(0, answer(1L, true).getXpAwarded());
        assertEquals(10, storedProgress.getXpEarned());
        assertEquals(10, user.getTotalXp());
    }

    @Test
    void failingAndRetryingDoesNotFarmXp() {
        exercise(1L, 10);
        exercise(2L, 10);

        // First attempt: one right, one wrong -> 50%, not passed
        answer(1L, true);
        answer(2L, false);
        LessonCompletionResult firstAttempt = progressService.completeLesson(USER_ID, LESSON_ID);
        assertFalse(firstAttempt.isPassed());
        assertFalse(firstAttempt.isCompleted());
        assertEquals(10, firstAttempt.getLessonXpEarned());

        // Retry: exercise 1 already paid, exercise 2 pays for the first time
        progressService.startLessonAttempt(USER_ID, LESSON_ID);
        assertEquals(0, answer(1L, true).getXpAwarded());
        assertEquals(10, answer(2L, true).getXpAwarded());

        assertEquals(20, storedProgress.getXpEarned());
        assertEquals(20, user.getTotalXp());

        // A third run pays nothing at all
        progressService.startLessonAttempt(USER_ID, LESSON_ID);
        answer(1L, true);
        answer(2L, true);
        assertEquals(20, storedProgress.getXpEarned());
        assertEquals(20, user.getTotalXp());
    }

    @Test
    void completingALessonLocksXpSoPracticeRunsPayNothing() {
        exercise(1L, 10);
        exercise(2L, 10);

        answer(1L, true);
        answer(2L, true);
        LessonCompletionResult completion = progressService.completeLesson(USER_ID, LESSON_ID);

        assertTrue(completion.isPassed());
        assertTrue(completion.isCompleted());
        assertFalse(completion.isPracticeMode());
        assertEquals(100.0, completion.getScorePercentage());
        assertEquals(20, completion.getLessonXpEarned());
        assertTrue(storedProgress.isXpLocked());

        int xpAfterCompletion = user.getTotalXp();

        // Practice run
        progressService.startLessonAttempt(USER_ID, LESSON_ID);
        exercise(3L, 10);
        assertEquals(0, answer(3L, true).getXpAwarded());
        assertEquals(xpAfterCompletion, user.getTotalXp());

        LessonCompletionResult practice = progressService.completeLesson(USER_ID, LESSON_ID);
        assertTrue(practice.isPracticeMode());
        assertTrue(practice.isCompleted());
    }

    @Test
    void startingAnAttemptClearsCountersButKeepsEarnedXpAndCompletion() {
        exercise(1L, 10);
        answer(1L, true);

        Progress progress = progressService.startLessonAttempt(USER_ID, LESSON_ID);

        assertEquals(0, progress.getCorrectAnswers());
        assertEquals(0, progress.getTotalAnswers());
        assertEquals(0, progress.getCurrentStreak());
        assertEquals(10, progress.getXpEarned());
        assertTrue(progress.hasXpBeenAwardedFor(1L));
    }

    @Test
    void rejectsAnExerciseFromAnotherLesson() {
        Lesson otherLesson = new Lesson("Numbers", 1, "Counting");
        otherLesson.setId(999L);
        Exercise foreign = exercise(1L, 10);
        foreign.setLesson(otherLesson);

        assertThrows(IllegalArgumentException.class, () -> answer(1L, true));
    }
}
