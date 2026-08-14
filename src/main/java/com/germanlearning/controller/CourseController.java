package com.germanlearning.controller;

import com.germanlearning.dto.CourseContentDto;
import com.germanlearning.dto.CourseDto;
import com.germanlearning.dto.LessonStatusDto;
import com.germanlearning.dto.UnitDto;
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
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.Optional;

/**
 * Course listing and course content (units + lesson tiles).
 *
 * The dashboard endpoint reproduces exactly what DashboardView did: it renders
 * the first course of the catalogue.
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

    @GetMapping("/courses")
    public List<CourseDto> getCourses() {
        return lessonService.getAllCourses().stream().map(CourseDto::from).toList();
    }

    @GetMapping("/courses/{courseId}")
    public ResponseEntity<CourseContentDto> getCourseContent(@PathVariable Long courseId) {
        return lessonService.getCourseById(courseId)
                .map(course -> ResponseEntity.ok(buildContent(course)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** The dashboard: the first course with its units and lesson statuses. */
    @GetMapping("/dashboard")
    public ResponseEntity<CourseContentDto> getDashboard() {
        Optional<Course> firstCourse = lessonService.getAllCourses().stream().findFirst();
        return firstCourse
                .map(course -> ResponseEntity.ok(buildContent(course)))
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
