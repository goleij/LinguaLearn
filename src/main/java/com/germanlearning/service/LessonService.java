package com.germanlearning.service;

import com.germanlearning.model.*;
import com.germanlearning.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class LessonService {

    private final LessonRepository lessonRepository;
    private final UnitRepository unitRepository;
    private final CourseRepository courseRepository;
    private final ProgressRepository progressRepository;
    private final ExerciseRepository exerciseRepository;

    public LessonService(LessonRepository lessonRepository,
                         UnitRepository unitRepository,
                         CourseRepository courseRepository,
                         ProgressRepository progressRepository,
                         ExerciseRepository exerciseRepository) {
        this.lessonRepository = lessonRepository;
        this.unitRepository = unitRepository;
        this.courseRepository = courseRepository;
        this.progressRepository = progressRepository;
        this.exerciseRepository = exerciseRepository;
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public Optional<Course> getCourseById(Long courseId) {
        return courseRepository.findById(courseId);
    }

    public List<Unit> getUnitsForCourse(Long courseId) {
        return unitRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    public List<Lesson> getLessonsForUnit(Long unitId) {
        return lessonRepository.findByUnitIdOrderByOrderIndexAsc(unitId);
    }

    public Optional<Lesson> getLessonById(Long lessonId) {
        return lessonRepository.findById(lessonId);
    }

    public Optional<Lesson> getLessonWithExercises(Long lessonId) {
        return lessonRepository.findByIdWithExercises(lessonId);
    }

    public List<Exercise> getExercisesForLesson(Long lessonId) {
        return exerciseRepository.findByLessonIdOrderByIdAsc(lessonId);
    }

    public boolean isLessonUnlocked(Long userId, Long lessonId) {
        Optional<Lesson> lessonOpt = lessonRepository.findById(lessonId);
        if (lessonOpt.isEmpty()) {
            return false;
        }

        Lesson lesson = lessonOpt.get();
        
        if (lesson.getOrderIndex() == 0 && lesson.getUnit().getOrderIndex() == 0) {
            return true;
        }

        List<Lesson> allLessons = getAllLessonsOrdered(lesson.getUnit().getCourse().getId());
        int currentIndex = -1;
        for (int i = 0; i < allLessons.size(); i++) {
            if (allLessons.get(i).getId().equals(lessonId)) {
                currentIndex = i;
                break;
            }
        }

        if (currentIndex <= 0) {
            return true;
        }

        Lesson previousLesson = allLessons.get(currentIndex - 1);
        Optional<Progress> progressOpt = progressRepository.findByUserIdAndLessonId(userId, previousLesson.getId());
        
        return progressOpt.map(Progress::isCompleted).orElse(false);
    }

    public List<Lesson> getAllLessonsOrdered(Long courseId) {
        return lessonRepository.findAllByCourseIdOrdered(courseId);
    }

    public List<LessonStatus> getLessonsWithStatus(Long userId, Long courseId) {
        List<Lesson> allLessons = getAllLessonsOrdered(courseId);
        List<LessonStatus> result = new ArrayList<>();

        for (int i = 0; i < allLessons.size(); i++) {
            Lesson lesson = allLessons.get(i);
            Optional<Progress> progressOpt = progressRepository.findByUserIdAndLessonId(userId, lesson.getId());
            
            boolean completed = progressOpt.map(Progress::isCompleted).orElse(false);
            boolean unlocked;
            
            if (i == 0) {
                unlocked = true;
            } else {
                Lesson prevLesson = allLessons.get(i - 1);
                Optional<Progress> prevProgress = progressRepository.findByUserIdAndLessonId(userId, prevLesson.getId());
                unlocked = prevProgress.map(Progress::isCompleted).orElse(false);
            }

            double progress = progressOpt.map(Progress::getScorePercentage).orElse(0.0);
            
            result.add(new LessonStatus(lesson, unlocked, completed, progress));
        }

        return result;
    }

    public static class LessonStatus {
        private final Lesson lesson;
        private final boolean unlocked;
        private final boolean completed;
        private final double progressPercentage;

        public LessonStatus(Lesson lesson, boolean unlocked, boolean completed, double progressPercentage) {
            this.lesson = lesson;
            this.unlocked = unlocked;
            this.completed = completed;
            this.progressPercentage = progressPercentage;
        }

        public Lesson getLesson() {
            return lesson;
        }

        public boolean isUnlocked() {
            return unlocked;
        }

        public boolean isCompleted() {
            return completed;
        }

        public double getProgressPercentage() {
            return progressPercentage;
        }
    }
}
