package com.germanlearning.service;

import com.germanlearning.model.ActivityPhase;
import com.germanlearning.model.ActivityType;
import com.germanlearning.model.LessonActivity;
import com.germanlearning.repository.LessonActivityRepository;
import com.germanlearning.service.activity.ActivityGrader;
import com.germanlearning.service.activity.ActivityPayload;
import com.germanlearning.service.activity.GradingResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Reads lesson activities and grades answers by delegating to the grader
 * registered for the activity type.
 *
 * Says nothing about XP or progress: that stays in {@link ProgressService}.
 */
@Service
@Transactional(readOnly = true)
public class ActivityService {

    private final LessonActivityRepository activityRepository;
    private final Map<ActivityType, ActivityGrader> graders = new HashMap<>();

    public ActivityService(LessonActivityRepository activityRepository, List<ActivityGrader> graders) {
        this.activityRepository = activityRepository;
        for (ActivityGrader grader : graders) {
            for (ActivityType type : grader.supportedTypes()) {
                this.graders.put(type, grader);
            }
        }
    }

    public List<LessonActivity> getActivities(Long lessonId) {
        return activityRepository.findByLessonIdOrderByPositionAsc(lessonId);
    }

    public Optional<LessonActivity> getActivity(Long activityId) {
        return activityRepository.findById(activityId);
    }

    public int countCheckpointActivities(Long lessonId) {
        return activityRepository.countByLessonIdAndPhase(lessonId, ActivityPhase.CHECKPOINT);
    }

    public int countActivities(Long lessonId) {
        return activityRepository.countByLessonId(lessonId);
    }

    /**
     * Every German/English pair the lesson itself puts on the page, taken from
     * its vocabulary lists and its matching activities.
     *
     * This is what lets a missed word be filed for review without guessing:
     * the content already states both halves, so the pair is authored rather
     * than inferred.
     */
    public Map<String, String> getTaughtWordPairs(Long lessonId) {
        Map<String, String> pairs = new LinkedHashMap<>();

        for (LessonActivity activity : getActivities(lessonId)) {
            ActivityPayload payload = ActivityPayload.of(activity);

            for (Map<String, String> entry : payload.getObjectList("vocabulary")) {
                String german = entry.get("de");
                String english = entry.get("en");
                if (german != null && english != null) {
                    pairs.putIfAbsent(german, english);
                }
            }

            payload.getStringMap("pairs").forEach(pairs::putIfAbsent);
        }

        return pairs;
    }

    public GradingResult grade(LessonActivity activity, String answer) {
        if (!activity.isGraded()) {
            throw new IllegalArgumentException(
                    "Activity " + activity.getId() + " of type " + activity.getType() + " is not graded");
        }

        ActivityGrader grader = graders.get(activity.getType());
        if (grader == null) {
            throw new IllegalStateException("No grader registered for " + activity.getType());
        }

        return grader.grade(activity, answer);
    }
}
