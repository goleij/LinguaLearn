package com.germanlearning.controller;

import com.germanlearning.dto.AnswerRequest;
import com.germanlearning.dto.AnswerResultDto;
import com.germanlearning.dto.ErrorResponse;
import com.germanlearning.dto.LessonActivityDto;
import com.germanlearning.dto.LessonCompletionDto;
import com.germanlearning.dto.LessonDetailDto;
import com.germanlearning.dto.UserDto;
import com.germanlearning.model.ActivityPhase;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.LessonActivity;
import com.germanlearning.model.User;
import com.germanlearning.service.ActivityService;
import com.germanlearning.service.CurrentUserService;
import com.germanlearning.service.LessonService;
import com.germanlearning.service.ProgressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The lesson flow: load a lesson with its activities, start an attempt, answer
 * activities, finish.
 *
 * All XP and progress work is delegated to ProgressService, which stays the
 * single source of truth. The lock check is enforced here on every mutating
 * call so a locked lesson cannot be played by calling the API directly.
 */
@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final ActivityService activityService;
    private final ProgressService progressService;
    private final CurrentUserService currentUserService;

    public LessonController(LessonService lessonService,
            ActivityService activityService,
            ProgressService progressService,
            CurrentUserService currentUserService) {
        this.lessonService = lessonService;
        this.activityService = activityService;
        this.progressService = progressService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<?> getLesson(@PathVariable Long lessonId) {
        User user = currentUserService.requireCurrentUser();

        Optional<Lesson> lessonOpt = lessonService.getLessonById(lessonId);
        if (lessonOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse("Lesson not found"));
        }

        Lesson lesson = lessonOpt.get();
        boolean unlocked = lessonService.isLessonUnlocked(user.getId(), lessonId);

        // Locked lessons hand back no activities at all
        List<LessonActivity> activities = unlocked
                ? activityService.getActivities(lessonId)
                : List.of();

        Map<String, Integer> phaseCounts = new LinkedHashMap<>();
        for (ActivityPhase phase : ActivityPhase.values()) {
            phaseCounts.put(phase.name(), countPhase(activities, phase));
        }

        return ResponseEntity.ok(new LessonDetailDto(
                lesson.getId(),
                lesson.getName(),
                lesson.getDescription(),
                lesson.resolveCefrLevel() == null ? null : lesson.resolveCefrLevel().name(),
                unlocked,
                progressService.isLessonCompleted(user.getId(), lessonId),
                phaseCounts,
                activities.stream().map(LessonActivityDto::from).toList()));
    }

    /**
     * Starts a fresh attempt: clears the per-attempt counters, and counts the
     * learner as active today.
     *
     * Returns the user because that daily streak may have just moved, and the
     * navbar shows it. Reading it back rather than reusing the instance above
     * keeps the rule that the client is only ever handed stored values.
     */
    @PostMapping("/{lessonId}/attempt")
    public ResponseEntity<?> startAttempt(@PathVariable Long lessonId) {
        User user = currentUserService.requireCurrentUser();
        if (!lessonService.isLessonUnlocked(user.getId(), lessonId)) {
            return locked();
        }

        progressService.startLessonAttempt(user.getId(), lessonId);
        return ResponseEntity.ok(UserDto.from(currentUserService.requireCurrentUser()));
    }

    @PostMapping("/{lessonId}/activities/{activityId}/answer")
    public ResponseEntity<?> submitAnswer(@PathVariable Long lessonId,
            @PathVariable Long activityId,
            @RequestBody AnswerRequest request) {
        User user = currentUserService.requireCurrentUser();
        if (!lessonService.isLessonUnlocked(user.getId(), lessonId)) {
            return locked();
        }

        return ResponseEntity.ok(AnswerResultDto.from(
                progressService.submitAnswer(user.getId(), lessonId, activityId, request.answer())));
    }

    @PostMapping("/{lessonId}/complete")
    public ResponseEntity<?> completeLesson(@PathVariable Long lessonId) {
        User user = currentUserService.requireCurrentUser();
        if (!lessonService.isLessonUnlocked(user.getId(), lessonId)) {
            return locked();
        }

        return ResponseEntity.ok(LessonCompletionDto.from(
                progressService.completeLesson(user.getId(), lessonId),
                progressService.getPassThresholdPercentage()));
    }

    private int countPhase(List<LessonActivity> activities, ActivityPhase phase) {
        return (int) activities.stream().filter(activity -> activity.getPhase() == phase).count();
    }

    private ResponseEntity<ErrorResponse> locked() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("This lesson is locked!"));
    }
}
