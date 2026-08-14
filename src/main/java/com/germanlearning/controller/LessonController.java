package com.germanlearning.controller;

import com.germanlearning.dto.AnswerRequest;
import com.germanlearning.dto.AnswerResultDto;
import com.germanlearning.dto.ErrorResponse;
import com.germanlearning.dto.ExerciseDto;
import com.germanlearning.dto.LessonCompletionDto;
import com.germanlearning.dto.LessonDetailDto;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.User;
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
import java.util.List;
import java.util.Optional;

/**
 * The lesson flow: load a lesson, start an attempt, answer exercises, finish.
 *
 * All XP and progress work is delegated to ProgressService, which stays the
 * single source of truth. The lock check is enforced here on every mutating
 * call so a locked lesson cannot be played by calling the API directly.
 */
@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    private final LessonService lessonService;
    private final ProgressService progressService;
    private final CurrentUserService currentUserService;

    public LessonController(LessonService lessonService,
            ProgressService progressService,
            CurrentUserService currentUserService) {
        this.lessonService = lessonService;
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

        // Locked lessons hand back no exercises at all
        List<ExerciseDto> exercises = unlocked
                ? lessonService.getExercisesForLesson(lessonId).stream().map(ExerciseDto::from).toList()
                : List.of();

        return ResponseEntity.ok(new LessonDetailDto(
                lesson.getId(),
                lesson.getName(),
                lesson.getDescription(),
                unlocked,
                progressService.isLessonCompleted(user.getId(), lessonId),
                exercises));
    }

    /** Starts a fresh attempt: clears the per-attempt counters. */
    @PostMapping("/{lessonId}/attempt")
    public ResponseEntity<?> startAttempt(@PathVariable Long lessonId) {
        User user = currentUserService.requireCurrentUser();
        if (!lessonService.isLessonUnlocked(user.getId(), lessonId)) {
            return locked();
        }

        progressService.startLessonAttempt(user.getId(), lessonId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{lessonId}/exercises/{exerciseId}/answer")
    public ResponseEntity<?> submitAnswer(@PathVariable Long lessonId,
            @PathVariable Long exerciseId,
            @RequestBody AnswerRequest request) {
        User user = currentUserService.requireCurrentUser();
        if (!lessonService.isLessonUnlocked(user.getId(), lessonId)) {
            return locked();
        }

        return ResponseEntity.ok(AnswerResultDto.from(
                progressService.submitAnswer(user.getId(), lessonId, exerciseId, request.answer())));
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

    private ResponseEntity<ErrorResponse> locked() {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("This lesson is locked!"));
    }
}
