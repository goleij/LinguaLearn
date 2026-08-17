package com.germanlearning.service.activity;

import com.germanlearning.model.ActivityType;
import com.germanlearning.model.LessonActivity;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Matching activity. The answer arrives as "left:right;left:right" and is
 * correct only when every pair is right — but the feedback reports how many
 * were right and which one to look at again.
 */
@Component
public class MatchPairsGrader implements ActivityGrader {

    @Override
    public Set<ActivityType> supportedTypes() {
        return Set.of(ActivityType.MATCH_PAIRS);
    }

    @Override
    public GradingResult grade(LessonActivity activity, String answer) {
        Map<String, String> expectedPairs = ActivityPayload.of(activity).getStringMap("pairs");
        String expected = expectedPairs.entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .reduce((a, b) -> a + ", " + b)
                .orElse(activity.getCorrectAnswer());

        if (expectedPairs.isEmpty() || answer == null || answer.isBlank()) {
            return GradingResult.wrong(expected, "No pairs were submitted.");
        }

        int matched = 0;
        List<String> wrongLeftItems = new ArrayList<>();

        for (String pair : answer.split(";")) {
            String[] parts = pair.split(":", 2);
            if (parts.length != 2) {
                continue;
            }
            String left = AnswerText.normalize(parts[0]);
            String right = AnswerText.normalize(parts[1]);

            boolean pairCorrect = expectedPairs.entrySet().stream()
                    .anyMatch(entry -> AnswerText.normalize(entry.getKey()).equals(left)
                            && AnswerText.normalize(entry.getValue()).equals(right));

            if (pairCorrect) {
                matched++;
            } else {
                wrongLeftItems.add(parts[0].trim());
            }
        }

        if (matched == expectedPairs.size()) {
            return GradingResult.correct(expected, "Every pair matched.");
        }

        String feedback = matched + " of " + expectedPairs.size() + " pairs were right.";
        if (!wrongLeftItems.isEmpty()) {
            feedback += " Look again at: " + String.join(", ", wrongLeftItems) + ".";
        }
        return GradingResult.wrong(expected, feedback);
    }
}
