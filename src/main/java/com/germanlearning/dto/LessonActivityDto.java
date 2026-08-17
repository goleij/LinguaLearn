package com.germanlearning.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.germanlearning.model.LessonActivity;
import com.germanlearning.service.activity.ActivityPayload;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * An activity as the browser is allowed to see it.
 *
 * The correct answer, the explanation, the index of the right option and the
 * pair mapping stay on the server until an answer is submitted. Hints are sent
 * for guided practice, where offering help up front is the point.
 *
 * For listening the spoken text is sent on purpose (the browser has to say it)
 * which is why that type asks what the line means rather than for a
 * transcription.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record LessonActivityDto(
        Long id,
        String type,
        String phase,
        String phaseLabel,
        int position,
        String instruction,
        String prompt,
        String context,
        String hint,
        String skill,
        String difficulty,
        String cefrLevel,
        String topic,
        int xpReward,
        boolean graded,
        List<String> options,
        String sentence,
        List<String> words,
        List<String> leftItems,
        List<String> rightItems,
        List<Map<String, String>> lines,
        List<Map<String, String>> examples,
        List<Map<String, String>> vocabulary,
        List<String> bullets,
        String audioText,
        String audioLang,
        Integer minWords,
        List<String> mustUseWords,
        /** Word the "Real examples" activity looks up externally. */
        String exampleQuery) {

    public static LessonActivityDto from(LessonActivity activity) {
        ActivityPayload payload = ActivityPayload.of(activity);

        List<String> leftItems = null;
        List<String> rightItems = null;
        Map<String, String> pairs = payload.getStringMap("pairs");
        if (!pairs.isEmpty()) {
            leftItems = new ArrayList<>(pairs.keySet());
            // Shuffled server side so the mapping is never derivable from the order
            rightItems = new ArrayList<>(pairs.values());
            Collections.shuffle(rightItems);
        }

        // The hint helps during guided practice; elsewhere it would give it away
        String hint = activity.getPhase() != null && activity.getPhase().offersHintUpFront()
                ? activity.getHint()
                : null;

        return new LessonActivityDto(
                activity.getId(),
                activity.getType() == null ? null : activity.getType().name(),
                activity.getPhase() == null ? null : activity.getPhase().name(),
                activity.getPhase() == null ? null : activity.getPhase().getLabel(),
                activity.getPosition(),
                activity.getInstruction(),
                activity.getPrompt(),
                activity.getContext(),
                hint,
                activity.getSkill() == null ? null : activity.getSkill().name(),
                activity.getDifficulty() == null ? null : activity.getDifficulty().name(),
                activity.getCefrLevel() == null ? null : activity.getCefrLevel().name(),
                activity.getTopic(),
                activity.getXpReward(),
                activity.isGraded(),
                emptyToNull(payload.getStringList("options")),
                payload.getString("sentence"),
                emptyToNull(payload.getStringList("words")),
                leftItems,
                rightItems,
                emptyToNull(payload.getObjectList("lines")),
                emptyToNull(payload.getObjectList("examples")),
                emptyToNull(payload.getObjectList("vocabulary")),
                emptyToNull(payload.getStringList("bullets")),
                payload.getString("audioText"),
                payload.getString("audioLang"),
                payload.getInteger("minWords"),
                emptyToNull(payload.getStringList("mustUseWords")),
                payload.getString("exampleQuery"));
    }

    private static <T> List<T> emptyToNull(List<T> list) {
        return list == null || list.isEmpty() ? null : list;
    }
}
