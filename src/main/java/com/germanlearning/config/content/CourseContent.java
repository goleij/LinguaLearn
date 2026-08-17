package com.germanlearning.config.content;

import com.germanlearning.model.CefrLevel;
import com.germanlearning.model.Course;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.LessonActivity;
import com.germanlearning.model.Progress;
import com.germanlearning.model.Unit;
import com.germanlearning.repository.ActivityAttemptRepository;
import com.germanlearning.repository.CourseRepository;
import com.germanlearning.repository.LessonActivityRepository;
import com.germanlearning.repository.LessonRepository;
import com.germanlearning.repository.ProgressRepository;
import com.germanlearning.repository.UnitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

/**
 * One course worth of content.
 *
 * Seeding is a sync rather than an insert: courses, units and lessons are
 * matched by name and reused, and only the activities are rewritten. That way
 * a course that already exists — including one built from the old exercise
 * model — receives the authored lessons without losing its identity, and
 * therefore without losing the progress rows that point at its lessons.
 *
 * Running it twice changes nothing, because {@link #contentVersion()} decides
 * whether it runs at all.
 */
public abstract class CourseContent {

    @Autowired
    protected CourseRepository courseRepository;
    @Autowired
    protected UnitRepository unitRepository;
    @Autowired
    protected LessonRepository lessonRepository;
    @Autowired
    protected LessonActivityRepository activityRepository;
    @Autowired
    protected ActivityAttemptRepository attemptRepository;
    @Autowired
    protected ProgressRepository progressRepository;
    @Autowired
    protected ActivityFactory make;

    /** Used to find the course this content belongs to. */
    public abstract String courseName();

    /** Lower runs first. */
    public abstract int order();

    /**
     * Bump this whenever the authored content below changes, so installations
     * that already hold an older revision pick the new one up on next start.
     */
    public int contentVersion() {
        return 1;
    }

    public abstract void seed();

    // ------------------------------------------------------------- helpers

    /** Finds the course by name, creating it the first time. */
    protected Course course(String name, CefrLevel level, String description) {
        Course course = courseRepository.findByName(name).orElseGet(Course::new);
        course.setName(name);
        course.setLanguage("German");
        course.setLevel(level);
        course.setDescription(description);
        return courseRepository.save(course);
    }

    /** Finds the unit inside this course by name, creating it the first time. */
    protected Unit unit(Course course, String name, int orderIndex, String description) {
        Unit unit = unitRepository.findByCourseIdOrderByOrderIndexAsc(course.getId()).stream()
                .filter(existing -> name.equals(existing.getName()))
                .findFirst()
                .orElse(null);

        if (unit == null) {
            unit = new Unit(name, orderIndex, description);
            course.addUnit(unit);
        } else {
            unit.setOrderIndex(orderIndex);
            unit.setDescription(description);
        }

        return unitRepository.save(unit);
    }

    /** Finds the lesson inside this unit by name, creating it the first time. */
    protected Lesson lesson(Unit unit, String name, int orderIndex, String description, CefrLevel level) {
        Lesson lesson = lessonRepository.findByUnitIdOrderByOrderIndexAsc(unit.getId()).stream()
                .filter(existing -> name.equals(existing.getName()))
                .findFirst()
                .orElse(null);

        if (lesson == null) {
            lesson = new Lesson(name, orderIndex, description);
            unit.addLesson(lesson);
        } else {
            lesson.setOrderIndex(orderIndex);
            lesson.setDescription(description);
        }
        lesson.setCefrLevel(level);

        return lessonRepository.save(lesson);
    }

    /**
     * Installs the lesson's activities, replacing whatever was there before so
     * the lesson ends up with exactly this content and no duplicates.
     *
     * Deliberately works through the activity repository and the owning side of
     * the association only. Going through {@code lesson.getActivities()} would
     * mean clearing a cascaded, orphan-removing collection and repopulating it
     * in the same transaction, and Hibernate then removes the freshly inserted
     * rows again when it processes the collection at commit.
     */
    protected void add(Lesson lesson, LessonActivity... activities) {
        removeExistingActivities(lesson);

        int position = 0;
        for (LessonActivity activity : activities) {
            activity.setLesson(lesson);
            activity.setPosition(position++);
            activityRepository.save(activity);
        }
        activityRepository.flush();
    }

    /**
     * Clears the old activities of a lesson together with the two things that
     * point at them: the answer history, and the per-activity XP bookkeeping on
     * progress rows. Everything else on the progress row — earned XP,
     * completion, best score, attempts — is left alone.
     */
    private void removeExistingActivities(Lesson lesson) {
        List<LessonActivity> existing =
                activityRepository.findByLessonIdOrderByPositionAsc(lesson.getId());
        if (existing.isEmpty()) {
            return;
        }

        attemptRepository.deleteByActivityIdIn(existing.stream().map(LessonActivity::getId).toList());
        attemptRepository.flush();

        // The stored ids would otherwise point at activities that no longer
        // exist, and could collide with ids handed out to the new ones
        for (Progress progress : progressRepository.findByLessonId(lesson.getId())) {
            progress.setXpAwardedActivityIds(null);
            progressRepository.save(progress);
        }

        activityRepository.deleteAll(existing);
        activityRepository.flush();
    }
}
