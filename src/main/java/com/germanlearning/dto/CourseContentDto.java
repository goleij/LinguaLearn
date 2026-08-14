package com.germanlearning.dto;

import java.util.List;

/** Everything the dashboard needs for one course: the course and its units. */
public record CourseContentDto(CourseDto course, List<UnitDto> units) {
}
