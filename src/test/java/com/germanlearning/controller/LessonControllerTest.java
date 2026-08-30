package com.germanlearning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.germanlearning.config.SecurityConfig;
import com.germanlearning.model.ActivityPhase;
import com.germanlearning.model.CefrLevel;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.Skill;
import com.germanlearning.model.User;
import com.germanlearning.service.ActivityService;
import com.germanlearning.service.CurrentUserService;
import com.germanlearning.service.LessonService;
import com.germanlearning.service.ProgressService;
import com.germanlearning.service.ProgressService.AnswerResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The lock is what stops a learner from skipping ahead, and the controller is
 * the only place it is enforced: the services below it will happily grade an
 * answer for any lesson. So the tests that matter most here are the ones that
 * call the API the way a script would, straight at a lesson that has not been
 * unlocked yet.
 */
@WebMvcTest(LessonController.class)
@Import(SecurityConfig.class)
class LessonControllerTest {

    private static final long USER_ID = 1L;
    private static final long LESSON_ID = 42L;
    private static final long ACTIVITY_ID = 7L;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private ActivityService activityService;

    @MockBean
    private ProgressService progressService;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @BeforeEach
    void setUp() {
        User anna = new User("anna", "anna@example.com", "hash");
        anna.setId(USER_ID);
        when(currentUserService.requireCurrentUser()).thenReturn(anna);
        when(currentUserService.getCurrentUser()).thenReturn(Optional.of(anna));
    }

    // --------------------------------------------------------------- reading

    @Test
    void anUnknownLessonIsNotFound() throws Exception {
        when(lessonService.getLessonById(LESSON_ID)).thenReturn(Optional.empty());

        mockMvc.perform(signedIn(get("/api/lessons/" + LESSON_ID)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Lesson not found"));
    }

    @Test
    void aLockedLessonIsDescribedButHandsBackNoActivities() throws Exception {
        when(lessonService.getLessonById(LESSON_ID)).thenReturn(Optional.of(lesson()));
        when(lessonService.isLessonUnlocked(USER_ID, LESSON_ID)).thenReturn(false);

        mockMvc.perform(signedIn(get("/api/lessons/" + LESSON_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Greetings"))
                .andExpect(jsonPath("$.unlocked").value(false))
                .andExpect(jsonPath("$.activities").isEmpty());

        // Never even read, so a locked lesson cannot leak its answers
        verify(activityService, never()).getActivities(anyLong());
    }

    // ------------------------------------------------------------- the lock

    @Test
    void startingAnAttemptOnALockedLessonIsForbidden() throws Exception {
        lockTheLesson();

        mockMvc.perform(signedIn(post("/api/lessons/" + LESSON_ID + "/attempt")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This lesson is locked!"));

        verifyNoInteractions(progressService);
    }

    @Test
    void answeringAnActivityOfALockedLessonIsForbidden() throws Exception {
        lockTheLesson();

        mockMvc.perform(answerRequest())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This lesson is locked!"));

        // No grading, no XP, no attempt recorded
        verifyNoInteractions(progressService);
    }

    @Test
    void completingALockedLessonIsForbidden() throws Exception {
        lockTheLesson();

        mockMvc.perform(signedIn(post("/api/lessons/" + LESSON_ID + "/complete")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("This lesson is locked!"));

        verifyNoInteractions(progressService);
    }

    // ----------------------------------------------------------- attempting

    @Test
    void startingAnAttemptHandsBackTheUserSoTheStreakBadgeStaysRight() throws Exception {
        unlockTheLesson();
        User afterTheStreakMoved = new User("anna", "anna@example.com", "hash");
        afterTheStreakMoved.setId(USER_ID);
        afterTheStreakMoved.setCurrentStreak(3);
        when(currentUserService.requireCurrentUser())
                .thenReturn(afterTheStreakMoved);

        mockMvc.perform(signedIn(post("/api/lessons/" + LESSON_ID + "/attempt")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("anna"))
                .andExpect(jsonPath("$.currentStreak").value(3));

        verify(progressService).startLessonAttempt(USER_ID, LESSON_ID);
    }

    // ------------------------------------------------------------ answering

    @Test
    void aValidAnswerComesBackWithExactlyWhatWasStored() throws Exception {
        unlockTheLesson();
        when(progressService.submitAnswer(USER_ID, LESSON_ID, ACTIVITY_ID, "1"))
                .thenReturn(storedResult());

        mockMvc.perform(answerRequest())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correct").value(true))
                .andExpect(jsonPath("$.correctAnswer").value("Hello"))
                .andExpect(jsonPath("$.feedback").value("That is the one."))
                .andExpect(jsonPath("$.phase").value("PRACTICE"))
                .andExpect(jsonPath("$.skill").value("VOCABULARY"))
                .andExpect(jsonPath("$.cefrLevel").value("A1"))
                .andExpect(jsonPath("$.topic").value("greetings"))
                .andExpect(jsonPath("$.xpAwarded").value(12))
                .andExpect(jsonPath("$.userTotalXp").value(112))
                .andExpect(jsonPath("$.currentStreak").value(3));
    }

    @Test
    void anActivityThatBelongsToAnotherLessonIsABadRequest() throws Exception {
        unlockTheLesson();
        String message = "Activity " + ACTIVITY_ID + " does not belong to lesson " + LESSON_ID;
        when(progressService.submitAnswer(anyLong(), anyLong(), anyLong(), any()))
                .thenThrow(new IllegalArgumentException(message));

        mockMvc.perform(answerRequest())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(message));
    }

    @Test
    void theLessonApiNeedsASession() throws Exception {
        mockMvc.perform(get("/api/lessons/" + LESSON_ID))
                .andExpect(status().isUnauthorized());
    }

    // --------------------------------------------------------------- helpers

    private MockHttpServletRequestBuilder answerRequest() throws Exception {
        String path = "/api/lessons/" + LESSON_ID + "/activities/" + ACTIVITY_ID + "/answer";
        return signedIn(post(path))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("answer", "1")));
    }

    /** Signs the request in and adds the CSRF token the SPA sends. */
    private MockHttpServletRequestBuilder signedIn(MockHttpServletRequestBuilder request) {
        return request.with(user("anna")).with(csrf());
    }

    private void lockTheLesson() {
        when(lessonService.isLessonUnlocked(USER_ID, LESSON_ID)).thenReturn(false);
    }

    private void unlockTheLesson() {
        when(lessonService.isLessonUnlocked(USER_ID, LESSON_ID)).thenReturn(true);
    }

    private Lesson lesson() {
        Lesson lesson = new Lesson("Greetings", 0, "Learn basic German greetings");
        lesson.setId(LESSON_ID);
        lesson.setCefrLevel(CefrLevel.A1);
        return lesson;
    }

    private AnswerResult storedResult() {
        return new AnswerResult(true, "Hello", "That is the one.",
                "The German word for Hello", null,
                ActivityPhase.PRACTICE, Skill.VOCABULARY, CefrLevel.A1, "greetings",
                12, 36, 112, 3, 3, 3, 0, 0);
    }
}
