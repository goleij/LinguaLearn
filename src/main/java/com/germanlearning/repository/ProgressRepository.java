package com.germanlearning.repository;

import com.germanlearning.model.Lesson;
import com.germanlearning.model.Progress;
import com.germanlearning.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Long> {

    Optional<Progress> findByUserAndLesson(User user, Lesson lesson);

    Optional<Progress> findByUserIdAndLessonId(Long userId, Long lessonId);

    List<Progress> findByUser(User user);

    List<Progress> findByUserId(Long userId);

    List<Progress> findByLessonId(Long lessonId);

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "lesson" })
    List<Progress> findByUserIdAndCompleted(Long userId, boolean completed);

    @Query("SELECT p FROM Progress p WHERE p.user.id = :userId AND p.lesson.unit.id = :unitId")
    List<Progress> findByUserIdAndUnitId(@Param("userId") Long userId, @Param("unitId") Long unitId);

    @Query("SELECT COUNT(p) FROM Progress p WHERE p.user.id = :userId AND p.completed = true")
    int countCompletedLessons(@Param("userId") Long userId);

    @Query("SELECT SUM(p.xpEarned) FROM Progress p WHERE p.user.id = :userId")
    Integer getTotalXpByUserId(@Param("userId") Long userId);
}
