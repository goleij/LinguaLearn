package com.germanlearning.service.activity;

import com.germanlearning.model.ActivityType;
import com.germanlearning.model.LessonActivity;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Free writing in the APPLY phase. There is no single right answer, so the
 * rule is deliberately simple: write enough, and use the words the task asks
 * for.
 *
 * Grammar and spelling are a separate concern, handled on demand by the
 * writing coach ({@code /api/writing/check}) so the learner can revise before
 * submitting. Replacing this rule with a richer assessment means changing this
 * class only.
 */
@Component
public class ShortWritingGrader implements ActivityGrader {

    private static final int DEFAULT_MIN_WORDS = 3;

    @Override
    public Set<ActivityType> supportedTypes() {
        return Set.of(ActivityType.SHORT_WRITING);
    }

    @Override
    public GradingResult grade(LessonActivity activity, String answer) {
        ActivityPayload payload = ActivityPayload.of(activity);
        Integer configuredMinWords = payload.getInteger("minWords");
        int minWords = configuredMinWords == null ? DEFAULT_MIN_WORDS : configuredMinWords;
        List<String> mustUseWords = payload.getStringList("mustUseWords");

        String normalized = AnswerText.normalize(answer);
        String expected = activity.getCorrectAnswer() == null
                ? "Write at least " + minWords + " words"
                : AnswerText.primary(activity.getCorrectAnswer());

        if (normalized.isEmpty()) {
            return GradingResult.wrong(expected, "Nothing was written yet.");
        }

        int wordCount = normalized.split(" ").length;
        if (wordCount < minWords) {
            return GradingResult.wrong(expected,
                    "A bit short: " + wordCount + " of at least " + minWords + " words.");
        }

        List<String> missing = new ArrayList<>();
        for (String required : mustUseWords) {
            if (!normalized.contains(AnswerText.normalize(required))) {
                missing.add(required);
            }
        }

        if (!missing.isEmpty()) {
            return GradingResult.wrong(expected,
                    "Still missing: " + String.join(", ", missing) + ".");
        }

        return GradingResult.correct(expected,
                "Well done — " + wordCount + " words using everything the task asked for.");
    }
}
