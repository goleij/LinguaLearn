package com.germanlearning.service;

import com.germanlearning.model.ActivityType;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.LessonActivity;
import com.germanlearning.model.SavedWord;
import com.germanlearning.model.User;
import com.germanlearning.model.WordSource;
import com.germanlearning.repository.SavedWordRepository;
import com.germanlearning.repository.UserRepository;
import com.germanlearning.service.activity.ActivityPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
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
 * The word bank and the rule that fills it.
 *
 * The interesting half is the automatic one: a missed word is only ever saved
 * when the lesson itself declared both halves of the pair, so the tests below
 * check that a pair is found from either side, that a matching activity only
 * files what was actually missed, and that an activity about something the
 * lesson never taught quietly files nothing rather than guessing.
 */
class VocabularyServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long LESSON_ID = 100L;

    private final Map<Long, SavedWord> stored = new LinkedHashMap<>();

    private SavedWordRepository savedWordRepository;
    private ActivityService activityService;
    private VocabularyService vocabularyService;

    private User user;
    private Lesson lesson;
    private long nextId;

    @BeforeEach
    void setUp() {
        savedWordRepository = mock(SavedWordRepository.class);
        UserRepository userRepository = mock(UserRepository.class);
        activityService = mock(ActivityService.class);
        vocabularyService = new VocabularyService(savedWordRepository, userRepository, activityService);

        stored.clear();
        nextId = 1L;

        user = new User("anna", "anna@example.com", "hash");
        user.setId(USER_ID);
        lesson = new Lesson("Greetings", 0, "Basic greetings");
        lesson.setId(LESSON_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));

        // A tiny in-memory stand-in for the saved_words table
        when(savedWordRepository.save(any(SavedWord.class))).thenAnswer(call -> {
            SavedWord word = call.getArgument(0);
            if (word.getId() == null) {
                word.setId(nextId++);
            }
            stored.put(word.getId(), word);
            return word;
        });
        when(savedWordRepository.findByUserIdAndGermanIgnoreCase(anyLong(), any()))
                .thenAnswer(call -> stored.values().stream()
                        .filter(word -> word.getGerman().equalsIgnoreCase(call.getArgument(1)))
                        .findFirst());
        when(savedWordRepository.findByIdAndUserId(anyLong(), anyLong()))
                .thenAnswer(call -> Optional.ofNullable(stored.get(call.<Long>getArgument(0))));
        when(savedWordRepository.findDue(anyLong(), any(LocalDateTime.class)))
                .thenAnswer(call -> new ArrayList<>(stored.values().stream()
                        .filter(SavedWord::isDue)
                        .toList()));

        // What the Greetings lesson puts on the page
        when(activityService.getTaughtWordPairs(LESSON_ID)).thenReturn(new LinkedHashMap<>(Map.of(
                "Hallo", "Hello",
                "Guten Morgen", "Good morning",
                "Tschüss", "Goodbye")));
    }

    // ---------------------------------------------------------- the bank

    @Test
    void aSavedWordStartsDueSoItIsAskedStraightAway() {
        SavedWord saved = vocabularyService.save(
                USER_ID, "Hallo", "Hello", WordSource.VOCABULARY, "greetings", "Greetings");

        assertEquals("Hallo", saved.getGerman());
        assertEquals("Hello", saved.getEnglish());
        assertEquals(0, saved.getBox());
        assertTrue(saved.isDue());
    }

    @Test
    void savingTheSameWordTwiceDoesNotResetTheSchedule() {
        SavedWord first = vocabularyService.save(
                USER_ID, "Hallo", "Hello", WordSource.VOCABULARY, null, null);
        vocabularyService.review(USER_ID, first.getId(), true);
        vocabularyService.review(USER_ID, first.getId(), true);
        int boxAfterTwoReviews = stored.get(first.getId()).getBox();

        SavedWord again = vocabularyService.save(
                USER_ID, "hallo", "Hello", WordSource.MISTAKE, null, null);

        assertEquals(first.getId(), again.getId());
        assertEquals(boxAfterTwoReviews, again.getBox());
        assertEquals(1, stored.size());
    }

    @Test
    void aWordWithoutAMeaningIsRefused() {
        assertThrows(IllegalArgumentException.class,
                () -> vocabularyService.save(USER_ID, "Hallo", "  ", WordSource.MANUAL, null, null));
        assertThrows(IllegalArgumentException.class,
                () -> vocabularyService.save(USER_ID, " ", "Hello", WordSource.MANUAL, null, null));
    }

    // ------------------------------------------------------ the schedule

    @Test
    void rememberingAWordMovesItUpABoxAndPushesItIntoTheFuture() {
        SavedWord word = vocabularyService.save(
                USER_ID, "Hallo", "Hello", WordSource.VOCABULARY, null, null);

        SavedWord reviewed = vocabularyService.review(USER_ID, word.getId(), true);

        assertEquals(1, reviewed.getBox());
        assertFalse(reviewed.isDue(), "a remembered word should not be due again today");
        assertEquals(1, reviewed.getCorrectCount());
    }

    @Test
    void forgettingAWordSendsItBackToTheStartAndMakesItDueAgain() {
        SavedWord word = vocabularyService.save(
                USER_ID, "Hallo", "Hello", WordSource.VOCABULARY, null, null);
        vocabularyService.review(USER_ID, word.getId(), true);
        vocabularyService.review(USER_ID, word.getId(), true);

        SavedWord forgotten = vocabularyService.review(USER_ID, word.getId(), false);

        assertEquals(0, forgotten.getBox());
        assertTrue(forgotten.isDue());
    }

    @Test
    void aWordIsLearnedOnceItReachesTheLastBox() {
        SavedWord word = vocabularyService.save(
                USER_ID, "Hallo", "Hello", WordSource.VOCABULARY, null, null);

        for (int i = 0; i < SavedWord.LAST_BOX; i++) {
            vocabularyService.review(USER_ID, word.getId(), true);
        }

        assertTrue(stored.get(word.getId()).isLearned());
        // And it does not climb past the last box
        vocabularyService.review(USER_ID, word.getId(), true);
        assertEquals(SavedWord.LAST_BOX, stored.get(word.getId()).getBox());
    }

    // ------------------------------------------------- the automatic path

    @Test
    void aMissedWordIsFoundFromTheEnglishSideOfWhatTheLessonTaught() {
        // "What does 'Hallo' mean?" — the correct answer is the English
        LessonActivity activity = choice("Hello");

        vocabularyService.captureMistake(USER_ID, activity, "0");

        SavedWord saved = onlySavedWord();
        assertEquals("Hallo", saved.getGerman());
        assertEquals("Hello", saved.getEnglish());
        assertEquals(WordSource.MISTAKE, saved.getSource());
        assertEquals("Greetings", saved.getLessonName());
    }

    @Test
    void aMissedWordIsAlsoFoundFromTheGermanSide() {
        // "___ (Good morning)" — the correct answer is the German
        LessonActivity activity = choice("Guten Morgen");

        vocabularyService.captureMistake(USER_ID, activity, "Guten Tag");

        SavedWord saved = onlySavedWord();
        assertEquals("Guten Morgen", saved.getGerman());
        assertEquals("Good morning", saved.getEnglish());
    }

    @Test
    void umlautsDoNotStopTheWordBeingRecognised() {
        LessonActivity activity = choice("Tschuess");

        vocabularyService.captureMistake(USER_ID, activity, "Hallo");

        assertEquals("Tschüss", onlySavedWord().getGerman());
    }

    @Test
    void anActivityAboutSomethingTheLessonNeverTaughtSavesNothing() {
        LessonActivity activity = choice("Auf Wiedersehen");

        vocabularyService.captureMistake(USER_ID, activity, "Hallo");

        assertTrue(stored.isEmpty(), "a pair that was never taught must not be guessed at");
    }

    @Test
    void aMatchingActivityOnlyFilesThePairsThatWereActuallyMissed() {
        LessonActivity activity = matchPairs();

        // Hallo was right; the other two were swapped
        vocabularyService.captureMistake(USER_ID, activity,
                "Hallo:Hello;Danke:Goodbye;Tschüss:Thank you");

        List<String> saved = stored.values().stream().map(SavedWord::getGerman).sorted().toList();
        assertEquals(List.of("Danke", "Tschüss"), saved);
    }

    @Test
    void aLessonThatTeachesNoWordPairsSavesNothing() {
        when(activityService.getTaughtWordPairs(LESSON_ID)).thenReturn(new LinkedHashMap<>());

        vocabularyService.captureMistake(USER_ID, choice("Hello"), "0");

        assertTrue(stored.isEmpty());
    }

    @Test
    void aFailureWhileFilingAWordNeverReachesTheLearner() {
        when(activityService.getTaughtWordPairs(LESSON_ID))
                .thenThrow(new IllegalStateException("the database is gone"));

        // Grading an answer must not fail because a nicety did
        vocabularyService.captureMistake(USER_ID, choice("Hello"), "0");

        assertTrue(stored.isEmpty());
    }

    // --------------------------------------------------------------- setup

    private SavedWord onlySavedWord() {
        assertEquals(1, stored.size(), "expected exactly one word to be filed");
        return stored.values().iterator().next();
    }

    private LessonActivity choice(String correctAnswer) {
        LessonActivity activity = new LessonActivity();
        activity.setId(nextId++);
        activity.setLesson(lesson);
        activity.setType(ActivityType.MULTIPLE_CHOICE);
        activity.setTopic("greetings");
        activity.setCorrectAnswer(correctAnswer);
        return activity;
    }

    private LessonActivity matchPairs() {
        LessonActivity activity = new LessonActivity();
        activity.setId(nextId++);
        activity.setLesson(lesson);
        activity.setType(ActivityType.MATCH_PAIRS);
        activity.setTopic("greetings");

        Map<String, String> pairs = new LinkedHashMap<>();
        pairs.put("Hallo", "Hello");
        pairs.put("Danke", "Thank you");
        pairs.put("Tschüss", "Goodbye");
        activity.setPayload(ActivityPayload.write("pairs", pairs));

        return activity;
    }
}
