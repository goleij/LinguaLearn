package com.germanlearning.service;

import com.germanlearning.model.ActivityType;
import com.germanlearning.model.LessonActivity;
import com.germanlearning.model.SavedWord;
import com.germanlearning.model.User;
import com.germanlearning.model.WordSource;
import com.germanlearning.repository.SavedWordRepository;
import com.germanlearning.repository.UserRepository;
import com.germanlearning.service.activity.ActivityPayload;
import com.germanlearning.service.activity.AnswerText;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The learner's personal word bank and its review schedule.
 *
 * Words arrive two ways. The learner can add one deliberately, from the Word
 * Explorer or from a lesson's vocabulary list. Or a word adds itself: when an
 * answer is wrong and the word behind it is one the lesson actually taught, it
 * goes into the bank so it comes back in review. That second path is
 * deliberately conservative — it only ever stores a pair the content itself
 * declared, never one guessed out of a prompt.
 */
@Service
@Transactional
public class VocabularyService {

    private static final Logger log = LoggerFactory.getLogger(VocabularyService.class);

    private final SavedWordRepository savedWordRepository;
    private final UserRepository userRepository;
    private final ActivityService activityService;

    public VocabularyService(SavedWordRepository savedWordRepository,
            UserRepository userRepository,
            ActivityService activityService) {
        this.savedWordRepository = savedWordRepository;
        this.userRepository = userRepository;
        this.activityService = activityService;
    }

    // ------------------------------------------------------------ the bank

    @Transactional(readOnly = true)
    public List<SavedWord> list(Long userId) {
        return savedWordRepository.findByUserIdOrderByAddedAtDesc(userId);
    }

    /** Everything whose review date has come. */
    @Transactional(readOnly = true)
    public List<SavedWord> due(Long userId) {
        return savedWordRepository.findDue(userId, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Stats stats(Long userId) {
        long total = savedWordRepository.countByUserId(userId);
        long learned = savedWordRepository.countByUserIdAndBoxGreaterThanEqual(userId, SavedWord.LAST_BOX);
        long due = savedWordRepository.findDue(userId, LocalDateTime.now()).size();
        return new Stats(total, due, learned);
    }

    /**
     * Adds a word, or returns the one already there. Saving the same word twice
     * must never reset a review schedule the learner has built up, so an
     * existing entry is only ever given a better translation, not a new box.
     */
    public SavedWord save(Long userId, String german, String english, WordSource source,
            String topic, String lessonName) {
        String cleanedGerman = german == null ? "" : german.trim();
        String cleanedEnglish = english == null ? "" : english.trim();

        if (cleanedGerman.isEmpty()) {
            throw new IllegalArgumentException("A word is needed");
        }
        if (cleanedEnglish.isEmpty()) {
            throw new IllegalArgumentException("A meaning is needed");
        }

        Optional<SavedWord> existing =
                savedWordRepository.findByUserIdAndGermanIgnoreCase(userId, cleanedGerman);
        if (existing.isPresent()) {
            SavedWord word = existing.get();
            if (word.getTopic() == null && topic != null) {
                word.setTopic(topic);
            }
            if (word.getLessonName() == null && lessonName != null) {
                word.setLessonName(lessonName);
            }
            return savedWordRepository.save(word);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        SavedWord word = new SavedWord(user, cleanedGerman, cleanedEnglish, source);
        word.setTopic(topic);
        word.setLessonName(lessonName);
        return savedWordRepository.save(word);
    }

    public SavedWord review(Long userId, Long wordId, boolean remembered) {
        SavedWord word = savedWordRepository.findByIdAndUserId(wordId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Word not found"));

        word.review(remembered);
        return savedWordRepository.save(word);
    }

    public void remove(Long userId, Long wordId) {
        SavedWord word = savedWordRepository.findByIdAndUserId(wordId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Word not found"));

        savedWordRepository.delete(word);
    }

    // ------------------------------------------------- the automatic path

    /**
     * Called after a wrong answer. Finds the word the activity was really about
     * and files it for review.
     *
     * The pair is never parsed out of the prompt. It is looked up in what the
     * lesson taught: the vocabulary lists and matching pairs of that same
     * lesson already hold German and English side by side, so matching the
     * activity's correct answer against either side gives a pair that is right
     * by construction rather than by guesswork. A word that cannot be resolved
     * that way is simply not saved.
     */
    public void captureMistake(Long userId, LessonActivity activity, String answer) {
        try {
            Long lessonId = activity.getLesson() == null ? null : activity.getLesson().getId();
            if (lessonId == null) {
                return;
            }

            Map<String, String> taught = activityService.getTaughtWordPairs(lessonId);
            if (taught.isEmpty()) {
                return;
            }

            String lessonName = activity.getLesson().getName();
            String topic = activity.getTopic();

            if (activity.getType() == ActivityType.MATCH_PAIRS) {
                for (Map.Entry<String, String> missed : missedPairs(activity, answer).entrySet()) {
                    saveFromMistake(userId, missed.getKey(), missed.getValue(), topic, lessonName);
                }
                return;
            }

            findPair(taught, activity.getCorrectAnswer())
                    .ifPresent(pair -> saveFromMistake(
                            userId, pair.getKey(), pair.getValue(), topic, lessonName));
        } catch (RuntimeException e) {
            // Filing a word is a nicety; it must never cost the learner an answer
            log.warn("Could not add the missed word to the word bank: {}", e.toString());
        }
    }

    private void saveFromMistake(Long userId, String german, String english,
            String topic, String lessonName) {
        save(userId, german, english, WordSource.MISTAKE, topic, lessonName);
    }

    /** Which pairs of a matching activity the learner did not get right. */
    private Map<String, String> missedPairs(LessonActivity activity, String answer) {
        Map<String, String> expected = ActivityPayload.of(activity).getStringMap("pairs");
        Map<String, String> missed = new LinkedHashMap<>(expected);

        if (answer == null || answer.isBlank()) {
            return missed;
        }

        for (String submitted : answer.split(";")) {
            String[] parts = submitted.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            String left = AnswerText.normalize(parts[0]);
            String right = AnswerText.normalize(parts[1]);

            expected.entrySet().stream()
                    .filter(entry -> AnswerText.normalize(entry.getKey()).equals(left)
                            && AnswerText.normalize(entry.getValue()).equals(right))
                    .findFirst()
                    .ifPresent(entry -> missed.remove(entry.getKey()));
        }

        return missed;
    }

    /**
     * The correct answer of a vocabulary activity is one half of a pair the
     * lesson taught — sometimes the German ("Guten Morgen"), sometimes the
     * English ("Good morning"). Either side identifies the same pair.
     */
    private Optional<Map.Entry<String, String>> findPair(Map<String, String> taught, String correctAnswer) {
        if (correctAnswer == null || correctAnswer.isBlank()) {
            return Optional.empty();
        }
        String target = AnswerText.normalize(AnswerText.primary(correctAnswer));

        return taught.entrySet().stream()
                .filter(entry -> AnswerText.normalize(entry.getKey()).equals(target)
                        || AnswerText.normalize(entry.getValue()).equals(target))
                .findFirst();
    }

    /** Totals for the word bank header. */
    public record Stats(long total, long due, long learned) {
    }
}
