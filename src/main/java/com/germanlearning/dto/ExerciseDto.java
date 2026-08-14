package com.germanlearning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.germanlearning.model.Exercise;
import com.germanlearning.model.FillBlankExercise;
import com.germanlearning.model.MatchExercise;
import com.germanlearning.model.MultipleChoiceExercise;
import com.germanlearning.model.OrderExercise;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An exercise as the browser is allowed to see it.
 *
 * Deliberately withholds everything that would give the answer away: no
 * correctAnswer, no explanation, no correct option index, no correct word
 * order and no pair mapping. That data stays on the server until an answer is
 * submitted.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ExerciseDto(
        Long id,
        String type,
        String question,
        List<String> options,
        String sentenceTemplate,
        List<String> leftItems,
        List<String> rightItems,
        List<String> words) {

    public static ExerciseDto from(Exercise exercise) {
        String type = exercise.getExerciseType() != null ? exercise.getExerciseType().name() : null;

        if (exercise instanceof MultipleChoiceExercise mc) {
            return new ExerciseDto(mc.getId(), type, mc.getQuestion(),
                    Arrays.asList(mc.getOptionsArray()), null, null, null, null);
        }
        if (exercise instanceof FillBlankExercise fb) {
            return new ExerciseDto(fb.getId(), type, fb.getQuestion(),
                    null, fb.getSentenceWithBlank(), null, null, null);
        }
        if (exercise instanceof MatchExercise match) {
            Map<String, String> pairs = match.getPairs();
            List<String> leftItems = new ArrayList<>(pairs.keySet());
            // Shuffled server side so the mapping is never derivable from the order.
            List<String> rightItems = new ArrayList<>(pairs.values());
            Collections.shuffle(rightItems);
            return new ExerciseDto(match.getId(), type, match.getQuestion(),
                    null, null, leftItems, rightItems, null);
        }
        if (exercise instanceof OrderExercise order) {
            return new ExerciseDto(order.getId(), type, order.getQuestion(),
                    null, null, null, null, new ArrayList<>(order.getShuffledWordsList()));
        }

        return new ExerciseDto(exercise.getId(), type, exercise.getQuestion(),
                null, null, null, null, null);
    }
}
