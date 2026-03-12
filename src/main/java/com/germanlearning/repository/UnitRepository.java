package com.germanlearning.repository;

import com.germanlearning.model.Course;
import com.germanlearning.model.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UnitRepository extends JpaRepository<Unit, Long> {
    
    List<Unit> findByCourseOrderByOrderIndexAsc(Course course);
    
    List<Unit> findByCourseIdOrderByOrderIndexAsc(Long courseId);
}
