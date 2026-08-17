package com.germanlearning.dto;

import com.germanlearning.model.CefrLevel;

/**
 * A level the learner can choose, with how much content is behind it.
 */
public record CefrLevelDto(
        String level,
        String label,
        String description,
        int courseCount,
        int lessonCount) {

    public static CefrLevelDto from(CefrLevel level, int courseCount, int lessonCount) {
        return new CefrLevelDto(
                level.name(),
                level.getLabel(),
                level.getDescription(),
                courseCount,
                lessonCount);
    }
}
