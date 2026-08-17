package com.germanlearning.service;

import com.germanlearning.model.ActivityAttempt;
import com.germanlearning.model.ActivityPhase;
import com.germanlearning.model.ActivityType;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.LessonActivity;
import com.germanlearning.model.Progress;
import com.germanlearning.model.User;
import com.germanlearning.repository.ActivityAttemptRepository;
import com.germanlearning.repository.LessonActivityRepository;
import com.germanlearning.repository.LessonRepository;
import com.germanlearning.repository.ProgressRepository;
import com.germanlearning.repository.UserRepository;
import com.germanlearning.service.ProgressService.AnswerResult;
import com.germanlearning.service.ProgressService.LessonCompletionResult;
import com.germanlearning.service.activity.ActivityPayload;
import com.germanlearning.service.activity.ChoiceGrader;
import com.germanlearning.service.activity.MatchPairsGrader;
import com.germanlearning.service.activity.SentenceBuilderGrader;
import com.germanlearning.service.activity.ShortWritingGrader;
import com.germanlearning.service.activity.TextAnswerGrader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
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
 * Covers the XP rules and the phase rules of the lesson flow: one formula, a
 * real streak, no double payouts on retry, UI numbers equal to stored numbers,
 * and a checkpoint that alone decides completion.
 */
class ProgressServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long LESSON_ID = 100L;

    private ProgressRepository progressRepository;
    private UserRepository userRepository;
    private ProgressService progressService;

    private User user;
    private Lesson lesson;
    private final Map<Long, LessonActivity> activities = new LinkedHashMap<>();
    private Progress storedProgress;
    private long nextActivityId;

    @BeforeEach
    void setUp() {
        progressRepository = mock(ProgressRepository.class);
        userRepository = mock(UserRepository.class);
        LessonRepository lessonRepository = mock(LessonRepository.class);
        LessonActivityRepository activityRepository = mock(LessonActivityRepository.class);
        ActivityAttemptRepository attemptRepository = mock(ActivityAttemptRepository.class);

        user = new User("anna", "anna@example.com", "hash");
        user.setId(USER_ID);

        lesson = new Lesson("Greetings", 0, "Basic greetings");
        lesson.setId(LESSON_ID);

        activities.clear();
        storedProgress = null;
        nextActivityId = 1L;

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        when(lessonRepository.findById(LESSON_ID)).thenReturn(Optional.of(lesson));
        when(attemptRepository.save(any(ActivityAttempt.class))).thenAnswer(inv -> inv.getArgument(0));

        when(activityRepository.findById(anyLong()))
                .thenAnswer(inv -> Optional.ofNullable(activities.get(inv.<Long>getArgument(0))));
        when(activityRepository.countByLessonIdAndPhase(anyLong(), any(ActivityPhase.class)))
                .thenAnswer(inv -> (int) activities.values().stream()
                        .filter(activity -> activity.getPhase() == inv.getArgument(1))
                        .count());

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

        ActivityService activityService = new ActivityService(activityRepository, List.of(
                new ChoiceGrader(),
                new TextAnswerGrader(),
                new SentenceBuilderGrader(),
                new MatchPairsGrader(),
                new ShortWritingGrader()));

        progressService = new ProgressService(progressRepository, userRepository, lessonRepository,
                attemptRepository, activityService, new ScoreService());
    }

    // ------------------------------------------------------------- fixtures

    private LessonActivity choice(ActivityPhase phase, int xpReward) {
        LessonActivity activity = new LessonActivity();
        activity.setId(nextActivityId++);
        activity.setLesson(lesson);
        activity.setType(ActivityType.MULTIPLE_CHOICE);
        activity.setPhase(phase);
        activity.setXpReward(xpReward);
        activity.setCorrectAnswer("Hello");
        activity.setPayload(ActivityPayload.write(
                "options", List.of("Bye", "Hello"), "correctIndex", 1));
        activities.put(activity.getId(), activity);
        return activity;
    }

    private LessonActivity learnCard() {
        LessonActivity activity = new LessonActivity();
        activity.setId(nextActivityId++);
        activity.setLesson(lesson);
        activity.setType(ActivityType.LEARN_CARD);
        activity.setPhase(ActivityPhase.LEARN);
        activity.setXpReward(0);
        activities.put(activity.getId(), activity);
        return activity;
    }

    private AnswerResult answer(LessonActivity activity, boolean correct) {
        return progressService.submitAnswer(USER_ID, LESSON_ID, activity.getId(), correct ? "1" : "0");
    }

    // ------------------------------------------------------------- XP rules

    @Test
    void awardsActivityRewardAndPersistsExactlyWhatItReturns() {
        LessonActivity activity = choice(ActivityPhase.PRACTICE, 10);

        AnswerResult result = answer(activity, true);

        assertTrue(result.isCorrect());
        assertEquals(10, result.getXpAwarded());
        assertEquals(result.getXpAwarded(), storedProgress.getXpEarned());
        assertEquals(result.getLessonXpEarned(), storedProgress.getXpEarned());
        assertEquals(result.getUserTotalXp(), user.getTotalXp());
        assertEquals(10, user.getTotalXp());
    }

    @Test
    void wrongAnswerEarnsNothingAndResetsTheStreak() {
        LessonActivity first = choice(ActivityPhase.PRACTICE, 10);
        LessonActivity second = choice(ActivityPhase.PRACTICE, 10);
        LessonActivity third = choice(ActivityPhase.PRACTICE, 10);

        answer(first, true);
        answer(second, true);
        AnswerResult wrong = answer(third, false);

        assertFalse(wrong.isCorrect());
        assertEquals(0, wrong.getXpAwarded());
        assertEquals(0, wrong.getCurrentStreak());
        assertEquals(20, storedProgress.getXpEarned());
        assertEquals(2, storedProgress.getCorrectAnswers());
        assertEquals(3, storedProgress.getTotalAnswers());
    }

    @Test
    void streakBonusFollowsConsecutiveCorrectAnswersNotTheCorrectCount() {
        List<LessonActivity> items = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            items.add(choice(ActivityPhase.PRACTICE, 10));
        }

        answer(items.get(0), true); // streak 1 -> 10
        answer(items.get(1), true); // streak 2 -> 10
        answer(items.get(2), false); // streak reset
        answer(items.get(3), true); // streak 1 -> 10, no bonus despite 3 correct
        AnswerResult fifth = answer(items.get(4), true); // streak 2 -> 10

        assertEquals(10, fifth.getXpAwarded());
        assertEquals(2, fifth.getCurrentStreak());
        assertEquals(40, storedProgress.getXpEarned());

        AnswerResult sixth = answer(items.get(5), true); // streak 3 -> bonus
        assertEquals(3, sixth.getCurrentStreak());
        assertEquals(15, sixth.getXpAwarded());
    }

    @Test
    void answeringTheSameActivityAgainPaysNothing() {
        LessonActivity activity = choice(ActivityPhase.PRACTICE, 10);

        assertEquals(10, answer(activity, true).getXpAwarded());
        assertEquals(0, answer(activity, true).getXpAwarded());
        assertEquals(10, storedProgress.getXpEarned());
        assertEquals(10, user.getTotalXp());
    }

    @Test
    void failingAndRetryingDoesNotFarmXp() {
        LessonActivity first = choice(ActivityPhase.CHECKPOINT, 10);
        LessonActivity second = choice(ActivityPhase.CHECKPOINT, 10);

        // First attempt: one right, one wrong -> 50% of the checkpoint, not passed
        answer(first, true);
        answer(second, false);
        LessonCompletionResult firstAttempt = progressService.completeLesson(USER_ID, LESSON_ID);
        assertFalse(firstAttempt.isPassed());
        assertFalse(firstAttempt.isCompleted());
        assertEquals(10, firstAttempt.getLessonXpEarned());

        // Retry: the first activity already paid, the second pays for the first time
        progressService.startLessonAttempt(USER_ID, LESSON_ID);
        assertEquals(0, answer(first, true).getXpAwarded());
        assertEquals(10, answer(second, true).getXpAwarded());

        assertEquals(20, storedProgress.getXpEarned());
        assertEquals(20, user.getTotalXp());

        // A third run pays nothing at all
        progressService.startLessonAttempt(USER_ID, LESSON_ID);
        answer(first, true);
        answer(second, true);
        assertEquals(20, storedProgress.getXpEarned());
        assertEquals(20, user.getTotalXp());
    }

    @Test
    void completingALessonLocksXpSoPracticeRunsPayNothing() {
        LessonActivity first = choice(ActivityPhase.CHECKPOINT, 10);
        LessonActivity second = choice(ActivityPhase.CHECKPOINT, 10);

        answer(first, true);
        answer(second, true);
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
        LessonActivity third = choice(ActivityPhase.CHECKPOINT, 10);
        assertEquals(0, answer(third, true).getXpAwarded());
        assertEquals(xpAfterCompletion, user.getTotalXp());

        LessonCompletionResult practice = progressService.completeLesson(USER_ID, LESSON_ID);
        assertTrue(practice.isPracticeMode());
        assertTrue(practice.isCompleted());
    }

    @Test
    void startingAnAttemptClearsCountersButKeepsEarnedXpAndCompletion() {
        LessonActivity activity = choice(ActivityPhase.CHECKPOINT, 10);
        answer(activity, true);

        Progress progress = progressService.startLessonAttempt(USER_ID, LESSON_ID);

        assertEquals(0, progress.getCorrectAnswers());
        assertEquals(0, progress.getTotalAnswers());
        assertEquals(0, progress.getCheckpointCorrectAnswers());
        assertEquals(0, progress.getCheckpointTotalAnswers());
        assertEquals(0, progress.getCurrentStreak());
        assertEquals(10, progress.getXpEarned());
        assertTrue(progress.hasXpBeenAwardedFor(activity.getId()));
    }

    @Test
    void rejectsAnActivityFromAnotherLesson() {
        Lesson otherLesson = new Lesson("Numbers", 1, "Counting");
        otherLesson.setId(999L);
        LessonActivity foreign = choice(ActivityPhase.PRACTICE, 10);
        foreign.setLesson(otherLesson);

        assertThrows(IllegalArgumentException.class, () -> answer(foreign, true));
    }

    @Test
    void rejectsAnswersForActivitiesThatAreNotGraded() {
        LessonActivity card = learnCard();

        assertThrows(IllegalArgumentException.class,
                () -> progressService.submitAnswer(USER_ID, LESSON_ID, card.getId(), "anything"));
    }

    // ---------------------------------------------------------- phase rules

    @Test
    void practiceMistakesDoNotFailTheLesson() {
        LessonActivity practice = choice(ActivityPhase.PRACTICE, 10);
        LessonActivity checkpoint = choice(ActivityPhase.CHECKPOINT, 10);

        answer(practice, false); // a mistake while practising
        answer(checkpoint, true); // checkpoint is perfect

        LessonCompletionResult completion = progressService.completeLesson(USER_ID, LESSON_ID);

        assertTrue(completion.isPassed());
        assertTrue(completion.isCompleted());
        assertEquals(100.0, completion.getScorePercentage());
        assertEquals(1, completion.getCheckpointCorrectAnswers());
        assertEquals(1, completion.getCheckpointTotalAnswers());
        // The overall counters still show both answers
        assertEquals(1, completion.getCorrectAnswers());
        assertEquals(2, completion.getTotalAnswers());
    }

    @Test
    void checkpointAloneDecidesCompletion() {
        LessonActivity practiceOne = choice(ActivityPhase.PRACTICE, 10);
        LessonActivity practiceTwo = choice(ActivityPhase.PRACTICE, 10);
        LessonActivity practiceThree = choice(ActivityPhase.PRACTICE, 10);
        LessonActivity checkpointOne = choice(ActivityPhase.CHECKPOINT, 10);
        LessonActivity checkpointTwo = choice(ActivityPhase.CHECKPOINT, 10);

        // Perfect practice, half the checkpoint
        answer(practiceOne, true);
        answer(practiceTwo, true);
        answer(practiceThree, true);
        answer(checkpointOne, true);
        answer(checkpointTwo, false);

        LessonCompletionResult completion = progressService.completeLesson(USER_ID, LESSON_ID);

        assertFalse(completion.isPassed());
        assertFalse(completion.isCompleted());
        assertEquals(50.0, completion.getScorePercentage());
        assertTrue(completion.isHasCheckpoint());
    }

    @Test
    void reachingTheCheckpointIsRequiredToComplete() {
        choice(ActivityPhase.CHECKPOINT, 10); // exists but never answered
        LessonActivity practice = choice(ActivityPhase.PRACTICE, 10);

        answer(practice, true);
        LessonCompletionResult completion = progressService.completeLesson(USER_ID, LESSON_ID);

        assertFalse(completion.isPassed());
        assertEquals(0, completion.getCheckpointTotalAnswers());
    }

    @Test
    void lessonWithoutCheckpointFallsBackToTheOverallScore() {
        LessonActivity first = choice(ActivityPhase.PRACTICE, 10);
        LessonActivity second = choice(ActivityPhase.PRACTICE, 10);

        answer(first, true);
        answer(second, true);

        LessonCompletionResult completion = progressService.completeLesson(USER_ID, LESSON_ID);

        assertFalse(completion.isHasCheckpoint());
        assertTrue(completion.isPassed());
        assertEquals(100.0, completion.getScorePercentage());
    }
}
