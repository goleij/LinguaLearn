package com.germanlearning.repository;

import com.germanlearning.model.CefrLevel;
import com.germanlearning.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByLanguage(String language);

    Optional<Course> findByNameAndLanguage(String name, String language);

    Optional<Course> findByName(String name);

    boolean existsByName(String name);

    List<Course> findByLevel(CefrLevel level);

    List<Course> findByLevelOrderByIdAsc(CefrLevel level);
}
