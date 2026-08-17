package com.germanlearning.service.activity;

import com.germanlearning.model.ActivityType;
import com.germanlearning.model.LessonActivity;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Set;

/**
 * Everything where the learner picks one option: plain multiple choice, the
 * situational variant, choosing a reply in a dialogue and choosing what a
 * spoken line meant.
 *
 * The answer is the index of the chosen option; the option text is accepted
 * too, which keeps older clients working.
 */
@Component
public class ChoiceGrader implements ActivityGrader {

    @Override
    public Set<ActivityType> supportedTypes() {
        return Set.of(
                ActivityType.MULTIPLE_CHOICE,
                ActivityType.CONTEXT_CHOICE,
                ActivityType.DIALOGUE,
                ActivityType.LISTENING);
    }

    @Override
    public GradingResult grade(LessonActivity activity, String answer) {
        ActivityPayload payload = ActivityPayload.of(activity);
        List<String> options = payload.getStringList("options");
        Integer correctIndex = payload.getInteger("correctIndex");

        String expected = activity.getCorrectAnswer();
        if (expected == null && correctIndex != null && correctIndex >= 0 && correctIndex < options.size()) {
            expected = options.get(correctIndex);
        }

        Integer chosenIndex = parseIndex(answer);
        boolean correct = chosenIndex != null && correctIndex != null
                ? chosenIndex.equals(correctIndex)
                : AnswerText.matches(answer, activity.getCorrectAnswer());

        if (correct) {
            return GradingResult.correct(expected, "That is the one.");
        }

        String chosen = chosenIndex != null && chosenIndex >= 0 && chosenIndex < options.size()
                ? options.get(chosenIndex)
                : null;

        String feedback = chosen == null
                ? "Have another look at the options."
                : "You picked \"" + chosen + "\". The right one here is \"" + expected + "\".";

        return GradingResult.wrong(expected, feedback);
    }

    private Integer parseIndex(String answer) {
        if (answer == null) {
            return null;
        }
        try {
            return Integer.valueOf(answer.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
