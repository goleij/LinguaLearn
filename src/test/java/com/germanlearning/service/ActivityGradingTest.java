package com.germanlearning.service;

import com.germanlearning.model.ActivityPhase;
import com.germanlearning.model.ActivityType;
import com.germanlearning.model.LessonActivity;
import com.germanlearning.repository.LessonActivityRepository;
import com.germanlearning.service.activity.ActivityPayload;
import com.germanlearning.service.activity.ChoiceGrader;
import com.germanlearning.service.activity.GradingResult;
import com.germanlearning.service.activity.MatchPairsGrader;
import com.germanlearning.service.activity.SentenceBuilderGrader;
import com.germanlearning.service.activity.ShortWritingGrader;
import com.germanlearning.service.activity.TextAnswerGrader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

/** The grading rules of each activity family. */
class ActivityGradingTest {

    private ActivityService activityService;

    @BeforeEach
    void setUp() {
        activityService = new ActivityService(mock(LessonActivityRepository.class), List.of(
                new ChoiceGrader(),
                new TextAnswerGrader(),
                new SentenceBuilderGrader(),
                new MatchPairsGrader(),
                new ShortWritingGrader()));
    }

    private LessonActivity activity(ActivityType type, String correctAnswer, String payload) {
        LessonActivity activity = new LessonActivity();
        activity.setId(1L);
        activity.setType(type);
        activity.setPhase(ActivityPhase.PRACTICE);
        activity.setCorrectAnswer(correctAnswer);
        activity.setPayload(payload);
        return activity;
    }

    @Test
    void choiceIsGradedByTheIndexOfTheChosenOption() {
        LessonActivity activity = activity(ActivityType.MULTIPLE_CHOICE, "Hello",
                ActivityPayload.write("options", List.of("Bye", "Hello"), "correctIndex", 1));

        assertTrue(activityService.grade(activity, "1").correct());
        assertFalse(activityService.grade(activity, "0").correct());
    }

    @Test
    void choiceAlsoAcceptsTheOptionText() {
        LessonActivity activity = activity(ActivityType.MULTIPLE_CHOICE, "Hello",
                ActivityPayload.write("options", List.of("Bye", "Hello"), "correctIndex", 1));

        assertTrue(activityService.grade(activity, "hello").correct());
    }

    @Test
    void fillBlankAcceptsAlternativesAndTypedUmlauts() {
        LessonActivity activity = activity(ActivityType.FILL_BLANK, "heiße",
                ActivityPayload.write("sentence", "Ich ___ Max."));

        assertTrue(activityService.grade(activity, "heiße").correct());
        assertTrue(activityService.grade(activity, "Heisse").correct());
        assertFalse(activityService.grade(activity, "komme").correct());
    }

    @Test
    void translationIgnoresCaseAndTrailingPunctuation() {
        LessonActivity activity = activity(ActivityType.TRANSLATION, "Ich komme aus Deutschland", null);

        assertTrue(activityService.grade(activity, "ich komme aus deutschland.").correct());
        assertFalse(activityService.grade(activity, "ich komme aus Berlin").correct());
    }

    @Test
    void sentenceBuilderChecksWordOrder() {
        LessonActivity activity = activity(ActivityType.SENTENCE_BUILDER, "Ich heiße Anna",
                ActivityPayload.write("words", List.of("heiße", "Anna", "Ich")));

        assertTrue(activityService.grade(activity, "Ich heiße Anna").correct());
        assertFalse(activityService.grade(activity, "Anna heiße Ich").correct());
    }

    @Test
    void matchPairsNeedsEveryPair() {
        Map<String, String> pairs = new LinkedHashMap<>();
        pairs.put("Hallo", "Hello");
        pairs.put("Danke", "Thank you");
        LessonActivity activity = activity(ActivityType.MATCH_PAIRS, null,
                ActivityPayload.write("pairs", pairs));

        assertTrue(activityService.grade(activity, "Hallo:Hello;Danke:Thank you").correct());
        assertFalse(activityService.grade(activity, "Hallo:Hello").correct());
        assertFalse(activityService.grade(activity, "Hallo:Thank you;Danke:Hello").correct());
    }

    @Test
    void shortWritingChecksLengthAndRequiredWords() {
        LessonActivity activity = activity(ActivityType.SHORT_WRITING, null,
                ActivityPayload.write("minWords", 3, "mustUseWords", List.of("ich", "bin")));

        assertTrue(activityService.grade(activity, "Ich bin müde").correct());
        assertFalse(activityService.grade(activity, "Ich bin").correct(), "too short");
        assertFalse(activityService.grade(activity, "Wir sind sehr glücklich").correct(),
                "missing the required words");
    }

    @Test
    void nonGradedActivitiesAreRejected() {
        LessonActivity card = activity(ActivityType.LEARN_CARD, null, null);

        assertThrows(IllegalArgumentException.class, () -> activityService.grade(card, "anything"));
    }

    @Test
    void theExpectedAnswerIsReportedWithoutTheAlternatives() {
        LessonActivity activity = activity(ActivityType.FILL_BLANK, "fünf|funf", null);

        GradingResult result = activityService.grade(activity, "vier");
        assertFalse(result.correct());
        assertEquals("fünf", result.expectedAnswer());
    }
}
