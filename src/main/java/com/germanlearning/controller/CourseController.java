package com.germanlearning.controller;

import com.germanlearning.dto.CefrLevelDto;
import com.germanlearning.dto.CourseContentDto;
import com.germanlearning.dto.CourseDto;
import com.germanlearning.dto.LessonStatusDto;
import com.germanlearning.dto.UnitDto;
import com.germanlearning.model.CefrLevel;
import com.germanlearning.model.Course;
import com.germanlearning.model.Unit;
import com.germanlearning.model.User;
import com.germanlearning.service.CurrentUserService;
import com.germanlearning.service.LessonService;
import com.germanlearning.service.LessonService.LessonStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Level selection and course content.
 *
 * The learner starts by choosing a CEFR level; everything below that is the
 * courses of that level with their units and lesson tiles.
 */
@RestController
@RequestMapping("/api")
public class CourseController {

    private final LessonService lessonService;
    private final CurrentUserService currentUserService;

    public CourseController(LessonService lessonService, CurrentUserService currentUserService) {
        this.lessonService = lessonService;
        this.currentUserService = currentUserService;
    }

    /** Every CEFR level with how much content it currently holds. */
    @GetMapping("/levels")
    public List<CefrLevelDto> getLevels() {
        List<Course> courses = lessonService.getAllCourses();
        List<CefrLevelDto> levels = new ArrayList<>();

        for (CefrLevel level : CefrLevel.values()) {
            List<Course> coursesOfLevel = courses.stream()
                    .filter(course -> course.getLevel() == level)
                    .toList();

            int lessonCount = coursesOfLevel.stream()
                    .mapToInt(course -> lessonService.getAllLessonsOrdered(course.getId()).size())
                    .sum();

            levels.add(CefrLevelDto.from(level, coursesOfLevel.size(), lessonCount));
        }

        return levels;
    }

    /** Courses, optionally narrowed to one level. */
    @GetMapping("/courses")
    public List<CourseDto> getCourses(@RequestParam(value = "level", required = false) CefrLevel level) {
        return lessonService.getAllCourses().stream()
                .filter(course -> level == null || course.getLevel() == level)
                .map(CourseDto::from)
                .toList();
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseContentDto> getCourseContent(@PathVariable Long courseId) {
        return lessonService.getCourseById(courseId)
                .map(course -> ResponseEntity.ok(buildContent(course)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * The dashboard: the first course of the requested level, or of the first
     * level that has content when none is given.
     */
    @GetMapping("/dashboard")
    public ResponseEntity<CourseContentDto> getDashboard(
            @RequestParam(value = "level", required = false) CefrLevel level) {
        List<Course> courses = lessonService.getAllCourses();

        Optional<Course> course = courses.stream()
                .filter(candidate -> level == null || candidate.getLevel() == level)
                .findFirst();

        return course
                .map(found -> ResponseEntity.ok(buildContent(found)))
                .orElseGet(() -> ResponseEntity.ok(null));
    }

    private CourseContentDto buildContent(Course course) {
        User user = currentUserService.requireCurrentUser();

        List<Unit> units = lessonService.getUnitsForCourse(course.getId());
        List<LessonStatus> allLessons = lessonService.getLessonsWithStatus(user.getId(), course.getId());

        List<UnitDto> unitDtos = units.stream()
                .map(unit -> new UnitDto(
                        unit.getId(),
                        unit.getName(),
                        unit.getDescription(),
                        unit.getOrderIndex(),
                        allLessons.stream()
                                .filter(status -> status.getLesson().getUnit().getId().equals(unit.getId()))
                                .map(LessonStatusDto::from)
                                .toList()))
                .toList();

        return new CourseContentDto(CourseDto.from(course), unitDtos);
    }
}
