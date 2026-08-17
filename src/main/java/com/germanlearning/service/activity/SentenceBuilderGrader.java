package com.germanlearning.service.activity;

import com.germanlearning.model.ActivityType;
import com.germanlearning.model.LessonActivity;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Set;
import java.util.TreeSet;

/**
 * The learner assembles words into a sentence; word order is what counts.
 *
 * When the words are right but the order is not, the feedback says so and
 * points at the first position that differs, which is the part a learner can
 * act on.
 */
@Component
public class SentenceBuilderGrader implements ActivityGrader {

    @Override
    public Set<ActivityType> supportedTypes() {
        return Set.of(ActivityType.SENTENCE_BUILDER);
    }

    @Override
    public GradingResult grade(LessonActivity activity, String answer) {
        String expected = AnswerText.primary(activity.getCorrectAnswer());

        if (AnswerText.matches(answer, activity.getCorrectAnswer())) {
            return GradingResult.correct(expected, "Correct word order.");
        }

        String[] answerWords = AnswerText.words(answer);
        String[] expectedWords = AnswerText.words(expected);

        boolean sameWords = new TreeSet<>(Arrays.asList(answerWords))
                .equals(new TreeSet<>(Arrays.asList(expectedWords)))
                && answerWords.length == expectedWords.length;

        if (sameWords) {
            int firstDifference = 0;
            while (firstDifference < answerWords.length
                    && answerWords[firstDifference].equals(expectedWords[firstDifference])) {
                firstDifference++;
            }
            return GradingResult.wrong(expected,
                    "All the right words, wrong order. Position " + (firstDifference + 1)
                            + " should be \"" + expectedWords[firstDifference] + "\".");
        }

        return GradingResult.wrong(expected, "Some words do not belong in this sentence.");
    }
}
