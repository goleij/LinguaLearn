package com.germanlearning.repository;

import com.germanlearning.model.ActivityPhase;
import com.germanlearning.model.LessonActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LessonActivityRepository extends JpaRepository<LessonActivity, Long> {

    List<LessonActivity> findByLessonIdOrderByPositionAsc(Long lessonId);

    List<LessonActivity> findByLessonIdAndPhaseOrderByPositionAsc(Long lessonId, ActivityPhase phase);

    int countByLessonIdAndPhase(Long lessonId, ActivityPhase phase);

    int countByLessonId(Long lessonId);
}
