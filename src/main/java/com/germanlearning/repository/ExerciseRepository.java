package com.germanlearning.repository;

import com.germanlearning.model.Exercise;
import com.germanlearning.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
    
    List<Exercise> findByLessonOrderByIdAsc(Lesson lesson);
    
    List<Exercise> findByLessonIdOrderByIdAsc(Long lessonId);
    
    int countByLessonId(Long lessonId);
}
