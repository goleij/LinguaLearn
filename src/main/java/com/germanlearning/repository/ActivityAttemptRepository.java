package com.germanlearning.repository;

import com.germanlearning.model.ActivityAttempt;
import com.germanlearning.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Collection;
import java.util.List;

/**
 * Answer history. The finder methods are the ones mistake tracking, adaptive
 * practice and Smart Review will need; nothing calls them yet.
 */
@Repository
public interface ActivityAttemptRepository extends JpaRepository<ActivityAttempt, Long> {

    List<ActivityAttempt> findTop50ByUserIdOrderByAnsweredAtDesc(Long userId);

    List<ActivityAttempt> findByUserIdAndCorrectFalseOrderByAnsweredAtDesc(Long userId);

    List<ActivityAttempt> findByUserIdAndSkillOrderByAnsweredAtDesc(Long userId, Skill skill);

    int countByUserIdAndCorrect(Long userId, boolean correct);

    /** Used when authored content is replaced and its activities disappear. */
    void deleteByActivityIdIn(Collection<Long> activityIds);
}
