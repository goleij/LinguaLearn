package com.germanlearning.repository;

import com.germanlearning.model.Lesson;
import com.germanlearning.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {
    
    List<Lesson> findByUnitOrderByOrderIndexAsc(Unit unit);
    
    List<Lesson> findByUnitIdOrderByOrderIndexAsc(Long unitId);
    
    @Query("SELECT l FROM Lesson l WHERE l.unit.course.id = :courseId ORDER BY l.unit.orderIndex, l.orderIndex")
    List<Lesson> findAllByCourseIdOrdered(@Param("courseId") Long courseId);
    
    @Query("SELECT l FROM Lesson l WHERE l.unit.id = :unitId AND l.orderIndex < :orderIndex ORDER BY l.orderIndex DESC")
    Optional<Lesson> findPreviousLesson(@Param("unitId") Long unitId, @Param("orderIndex") int orderIndex);
    
    @Query("SELECT l FROM Lesson l JOIN FETCH l.activities WHERE l.id = :lessonId")
    Optional<Lesson> findByIdWithActivities(@Param("lessonId") Long lessonId);
}
