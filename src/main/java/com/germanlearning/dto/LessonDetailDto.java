package com.germanlearning.dto;

import java.util.List;
import java.util.Map;

/**
 * A lesson ready to be played: its activities in order, plus how many belong
 * to each phase so the UI can show the Learn → Context → Guided practice →
 * Practice → Apply → Checkpoint progression. When the lesson is locked the
 * activities are withheld and the UI shows the "This lesson is locked!"
 * notification.
 */
public record LessonDetailDto(
        Long id,
        String name,
        String description,
        String cefrLevel,
        boolean unlocked,
        boolean practiceMode,
        /** Activity count per phase name, e.g. {"LEARN": 3, "PRACTICE": 4}. */
        Map<String, Integer> phaseCounts,
        List<LessonActivityDto> activities) {
}
