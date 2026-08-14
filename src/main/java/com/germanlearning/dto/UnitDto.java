package com.germanlearning.dto;

import java.util.List;

/** A unit card on the dashboard together with its lesson tiles. */
public record UnitDto(
        Long id,
        String name,
        String description,
        int orderIndex,
        List<LessonStatusDto> lessons) {
}
