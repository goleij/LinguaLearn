package com.germanlearning.config;

import com.germanlearning.config.content.CourseContent;
import com.germanlearning.model.Course;
import com.germanlearning.repository.CourseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Keeps the installed course content in step with the authored content.
 *
 * A course is not skipped just because it exists: each {@link CourseContent}
 * carries a version, and a course still holding an older revision — including
 * one built by the legacy exercise migration, which has no version at all — is
 * brought up to date. The content classes reuse the existing course, units and
 * lessons and rewrite only the activities, so users, progress and completion
 * survive and nothing is duplicated.
 *
 * Once a course is at the current version, later starts do nothing.
 */
@Component
@Order(20) // after SchemaMaintenance and LegacyExerciseMigrator
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final CourseRepository courseRepository;
    private final List<CourseContent> contents;

    public DataInitializer(CourseRepository courseRepository, List<CourseContent> contents) {
        this.courseRepository = courseRepository;
        this.contents = contents;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<CourseContent> ordered = contents.stream()
                .sorted(Comparator.comparingInt(CourseContent::order))
                .toList();

        for (CourseContent content : ordered) {
            Optional<Course> existing = courseRepository.findByName(content.courseName());

            // Missing course, or one from before versioning, both count as 0
            int installed = existing.map(Course::getContentVersion).orElse(0);

            if (installed >= content.contentVersion()) {
                continue;
            }

            content.seed();

            courseRepository.findByName(content.courseName()).ifPresent(course -> {
                course.setContentVersion(content.contentVersion());
                courseRepository.save(course);
            });

            log.info("Installed content v{} for course '{}' ({})",
                    content.contentVersion(), content.courseName(),
                    existing.isPresent() ? "refreshed" : "new");
        }
    }
}
