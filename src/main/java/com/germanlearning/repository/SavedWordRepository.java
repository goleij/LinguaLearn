package com.germanlearning.repository;

import com.germanlearning.model.SavedWord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SavedWordRepository extends JpaRepository<SavedWord, Long> {

    List<SavedWord> findByUserIdOrderByAddedAtDesc(Long userId);

    Optional<SavedWord> findByUserIdAndGermanIgnoreCase(Long userId, String german);

    Optional<SavedWord> findByIdAndUserId(Long id, Long userId);

    /** The review queue: everything whose date has come, oldest first. */
    @Query("SELECT w FROM SavedWord w WHERE w.user.id = :userId "
            + "AND (w.dueAt IS NULL OR w.dueAt <= :now) ORDER BY w.dueAt ASC, w.addedAt ASC")
    List<SavedWord> findDue(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    long countByUserId(Long userId);

    long countByUserIdAndBoxGreaterThanEqual(Long userId, int box);
}
