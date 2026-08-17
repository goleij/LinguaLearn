package com.germanlearning.service.activity;

import com.germanlearning.model.ActivityType;
import com.germanlearning.model.LessonActivity;
import org.springframework.stereotype.Component;
import java.util.Set;

/**
 * Free text with a known answer: filling a gap or translating a phrase.
 * Alternatives are listed on the activity separated by "|".
 *
 * A near miss is called out as such, because "almost — check the spelling" is
 * far more useful to a learner than "wrong".
 */
@Component
public class TextAnswerGrader implements ActivityGrader {

    @Override
    public Set<ActivityType> supportedTypes() {
        return Set.of(ActivityType.FILL_BLANK, ActivityType.TRANSLATION);
    }

    @Override
    public GradingResult grade(LessonActivity activity, String answer) {
        String expected = AnswerText.primary(activity.getCorrectAnswer());

        if (AnswerText.matches(answer, activity.getCorrectAnswer())) {
            return GradingResult.correct(expected, "Exactly right.");
        }

        if (AnswerText.isNearMiss(answer, activity.getCorrectAnswer())) {
            return GradingResult.wrong(expected,
                    "So close — check the spelling. You wrote \"" + answer.trim() + "\".");
        }

        if (activity.getType() == ActivityType.TRANSLATION) {
            String[] answerWords = AnswerText.words(answer);
            String[] expectedWords = AnswerText.words(expected);
            if (answerWords.length > 0 && answerWords.length != expectedWords.length) {
                return GradingResult.wrong(expected,
                        "Your sentence has " + answerWords.length + " words, the expected one has "
                                + expectedWords.length + ". Check what is missing or extra.");
            }
        }

        return GradingResult.wrong(expected, "Not this one — compare your answer with the solution.");
    }
}
