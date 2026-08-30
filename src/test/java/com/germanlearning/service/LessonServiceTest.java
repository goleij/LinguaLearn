package com.germanlearning.service;

import com.germanlearning.model.CefrLevel;
import com.germanlearning.model.Course;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.Progress;
import com.germanlearning.model.Unit;
import com.germanlearning.model.User;
import com.germanlearning.repository.CourseRepository;
import com.germanlearning.repository.LessonRepository;
import com.germanlearning.repository.ProgressRepository;
import com.germanlearning.repository.UnitRepository;
import com.germanlearning.service.LessonService.LessonStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The unlock chain: a course is one flat, ordered run of lessons across its
 * units, and a lesson opens only when the one before it has been completed.
 *
 * The two paths that answer this question — isLessonUnlocked for a single
 * lesson, and getLessonsWithStatus for the dashboard tiles — are separate
 * implementations of the same rule, so both are checked against the same
 * fixture. A course laid out as unit 0 [A, B] then unit 1 [C] is enough to
 * cover the interesting case, which is C: the first lesson of a unit, and
 * still locked, because its predecessor lives in the unit before it.
 */
class LessonServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long COURSE_ID = 10L;

    private LessonRepository lessonRepository;
    private ProgressRepository progressRepository;
    private LessonService lessonService;

    private User user;
    private Lesson first;
    private Lesson second;
    private Lesson thirdInTheNextUnit;

    @BeforeEach
    void setUp() {
        lessonRepository = mock(LessonRepository.class);
        UnitRepository unitRepository = mock(UnitRepository.class);
        CourseRepository courseRepository = mock(CourseRepository.class);
        progressRepository = mock(ProgressRepository.class);

        lessonService = new LessonService(
                lessonRepository, unitRepository, courseRepository, progressRepository);

        user = new User("anna", "anna@example.com", "hash");
        user.setId(USER_ID);

        Course course = new Course("German A1", "German", CefrLevel.A1, "Beginner German");
        course.setId(COURSE_ID);

        Unit basics = unit(course, "Basics", 0, 100L);
        Unit everyday = unit(course, "Everyday", 1, 101L);

        first = lesson(basics, "Greetings", 0, 1L);
        second = lesson(basics, "Introducing Yourself", 1, 2L);
        thirdInTheNextUnit = lesson(everyday, "In the Cafe", 0, 3L);

        List<Lesson> ordered = List.of(first, second, thirdInTheNextUnit);
        when(lessonRepository.findAllByCourseIdOrdered(COURSE_ID)).thenReturn(ordered);
        for (Lesson lesson : ordered) {
            when(lessonRepository.findById(lesson.getId())).thenReturn(Optional.of(lesson));
        }

        // Nobody has started anything yet
        when(progressRepository.findByUserIdAndLessonId(org.mockito.ArgumentMatchers.anyLong(),
                org.mockito.ArgumentMatchers.anyLong())).thenReturn(Optional.empty());
    }

    // -------------------------------------------------------- one lesson

    @Test
    void aLessonThatDoesNotExistIsNotUnlocked() {
        when(lessonRepository.findById(999L)).thenReturn(Optional.empty());

        assertFalse(lessonService.isLessonUnlocked(USER_ID, 999L));
    }

    @Test
    void theFirstLessonOfACourseIsAlwaysOpen() {
        assertTrue(lessonService.isLessonUnlocked(USER_ID, first.getId()));
    }

    @Test
    void aLaterLessonStaysLockedUntilTheOneBeforeItIsCompleted() {
        assertFalse(lessonService.isLessonUnlocked(USER_ID, second.getId()));
    }

    @Test
    void startingThePreviousLessonIsNotEnoughToUnlockTheNextOne() {
        havingProgressOn(first, false);

        assertFalse(lessonService.isLessonUnlocked(USER_ID, second.getId()));
    }

    @Test
    void completingThePreviousLessonUnlocksTheNextOne() {
        havingProgressOn(first, true);

        assertTrue(lessonService.isLessonUnlocked(USER_ID, second.getId()));
    }

    @Test
    void theChainCrossesUnitBoundaries() {
        // The first lesson of unit 1 waits for the last lesson of unit 0, not
        // for its own unit, so being first inside a unit unlocks nothing
        assertFalse(lessonService.isLessonUnlocked(USER_ID, thirdInTheNextUnit.getId()));

        havingProgressOn(second, true);

        assertTrue(lessonService.isLessonUnlocked(USER_ID, thirdInTheNextUnit.getId()));
    }

    @Test
    void completingALaterLessonDoesNotOpenTheOneAfterTheGap() {
        // Only the immediate predecessor counts
        havingProgressOn(first, true);

        assertTrue(lessonService.isLessonUnlocked(USER_ID, second.getId()));
        assertFalse(lessonService.isLessonUnlocked(USER_ID, thirdInTheNextUnit.getId()));
    }

    // ---------------------------------------------------- dashboard tiles

    @Test
    void theDashboardOpensOnlyTheFirstTileForANewLearner() {
        List<LessonStatus> tiles = lessonService.getLessonsWithStatus(USER_ID, COURSE_ID);

        assertEquals(3, tiles.size());
        assertTrue(tiles.get(0).isUnlocked());
        assertFalse(tiles.get(1).isUnlocked());
        assertFalse(tiles.get(2).isUnlocked());
        assertFalse(tiles.get(0).isCompleted());
    }

    @Test
    void theDashboardMovesTheOpenTileAlongAsLessonsAreCompleted() {
        havingProgressOn(first, true);

        List<LessonStatus> tiles = lessonService.getLessonsWithStatus(USER_ID, COURSE_ID);

        assertTrue(tiles.get(0).isCompleted());
        assertTrue(tiles.get(1).isUnlocked());
        assertFalse(tiles.get(1).isCompleted());
        assertFalse(tiles.get(2).isUnlocked());
    }

    @Test
    void theTilesAreInTheOrderTheCourseIsMeantToBePlayed() {
        List<String> names = new ArrayList<>();
        for (LessonStatus tile : lessonService.getLessonsWithStatus(USER_ID, COURSE_ID)) {
            names.add(tile.getLesson().getName());
        }

        assertEquals(List.of("Greetings", "Introducing Yourself", "In the Cafe"), names);
    }

    // --------------------------------------------------------------- setup

    private void havingProgressOn(Lesson lesson, boolean completed) {
        Progress progress = new Progress(user, lesson);
        progress.setCompleted(completed);
        when(progressRepository.findByUserIdAndLessonId(USER_ID, lesson.getId()))
                .thenReturn(Optional.of(progress));
    }

    private Unit unit(Course course, String name, int orderIndex, Long id) {
        Unit unit = new Unit(name, orderIndex, name);
        unit.setId(id);
        course.addUnit(unit);
        return unit;
    }

    private Lesson lesson(Unit unit, String name, int orderIndex, Long id) {
        Lesson lesson = new Lesson(name, orderIndex, name);
        lesson.setId(id);
        unit.addLesson(lesson);
        return lesson;
    }
}
