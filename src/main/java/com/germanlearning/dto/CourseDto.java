package com.germanlearning.dto;

import com.germanlearning.model.Course;

public record CourseDto(Long id, String name, String language, String level, String description) {

    public static CourseDto from(Course course) {
        return new CourseDto(
                course.getId(),
                course.getName(),
                course.getLanguage(),
                course.getLevel(),
                course.getDescription());
    }
}
