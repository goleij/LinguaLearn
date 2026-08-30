package com.germanlearning.controller;

import com.germanlearning.config.SecurityConfig;
import com.germanlearning.model.CefrLevel;
import com.germanlearning.model.Course;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.Unit;
import com.germanlearning.model.User;
import com.germanlearning.service.CurrentUserService;
import com.germanlearning.service.LessonService;
import com.germanlearning.service.LessonService.LessonStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * What the learner sees before a lesson starts: the level picker and the
 * dashboard behind it.
 *
 * The fixture holds one A1 course and one B1 course, which is enough to show
 * that a level is listed whether or not it has content, that the level filter
 * really filters, and that the dashboard hands back the lesson tiles with the
 * lock state already resolved.
 */
@WebMvcTest(CourseController.class)
@Import(SecurityConfig.class)
class CourseControllerTest {

    private static final Long USER_ID = 1L;
    private static final Long A1_COURSE_ID = 10L;
    private static final Long B1_COURSE_ID = 20L;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LessonService lessonService;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private AuthenticationManager authenticationManager;

    private Course a1;
    private Lesson greetings;
    private Lesson introductions;

    @BeforeEach
    void setUp() {
        User anna = new User("anna", "anna@example.com", "hash");
        anna.setId(USER_ID);
        when(currentUserService.requireCurrentUser()).thenReturn(anna);
        when(currentUserService.getCurrentUser()).thenReturn(Optional.of(anna));

        a1 = new Course("German A1", "German", CefrLevel.A1, "Beginner German");
        a1.setId(A1_COURSE_ID);
        Unit basics = new Unit("Basics", 0, "The first words");
        basics.setId(100L);
        a1.addUnit(basics);

        greetings = new Lesson("Greetings", 0, "Learn basic German greetings");
        greetings.setId(1L);
        greetings.setCefrLevel(CefrLevel.A1);
        basics.addLesson(greetings);

        introductions = new Lesson("Introducing Yourself", 1, "Say who you are");
        introductions.setId(2L);
        introductions.setCefrLevel(CefrLevel.A1);
        basics.addLesson(introductions);

        Course b1 = new Course("German B1", "German", CefrLevel.B1, "Intermediate German");
        b1.setId(B1_COURSE_ID);

        when(lessonService.getAllCourses()).thenReturn(List.of(a1, b1));
        when(lessonService.getAllLessonsOrdered(A1_COURSE_ID))
                .thenReturn(List.of(greetings, introductions));
        when(lessonService.getAllLessonsOrdered(B1_COURSE_ID)).thenReturn(List.of());
        when(lessonService.getUnitsForCourse(A1_COURSE_ID)).thenReturn(List.of(basics));
        when(lessonService.getCourseById(A1_COURSE_ID)).thenReturn(Optional.of(a1));
        when(lessonService.getCourseById(999L)).thenReturn(Optional.empty());
        when(lessonService.getLessonsWithStatus(USER_ID, A1_COURSE_ID)).thenReturn(List.of(
                new LessonStatus(greetings, true, true, 100.0),
                new LessonStatus(introductions, true, false, 0.0)));
    }

    // ---------------------------------------------------------- the levels

    @Test
    void everyCefrLevelIsOfferedEvenWhenItHoldsNothingYet() throws Exception {
        mockMvc.perform(signedIn(get("/api/levels")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0].level").value("A1"))
                .andExpect(jsonPath("$[0].label").value("Beginner"))
                .andExpect(jsonPath("$[0].courseCount").value(1))
                .andExpect(jsonPath("$[0].lessonCount").value(2))
                // A2 has no course at all, and is still offered, with zeroes
                .andExpect(jsonPath("$[1].level").value("A2"))
                .andExpect(jsonPath("$[1].courseCount").value(0))
                .andExpect(jsonPath("$[1].lessonCount").value(0))
                // B1 has a course but no lessons in it
                .andExpect(jsonPath("$[2].level").value("B1"))
                .andExpect(jsonPath("$[2].courseCount").value(1))
                .andExpect(jsonPath("$[2].lessonCount").value(0))
                .andExpect(jsonPath("$[3].level").value("B2"));
    }

    // --------------------------------------------------------- the courses

    @Test
    void everyCourseIsListedWhenNoLevelIsGiven() throws Exception {
        mockMvc.perform(signedIn(get("/api/courses")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void theLevelParameterNarrowsTheList() throws Exception {
        mockMvc.perform(signedIn(get("/api/courses").param("level", "A1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("German A1"))
                .andExpect(jsonPath("$[0].level").value("A1"))
                .andExpect(jsonPath("$[0].levelLabel").value("Beginner"));
    }

    @Test
    void aLevelWithNoCourseComesBackEmptyRatherThanFailing() throws Exception {
        mockMvc.perform(signedIn(get("/api/courses").param("level", "A2")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void anUnknownCourseIsNotFound() throws Exception {
        mockMvc.perform(signedIn(get("/api/courses/999")))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------- the dashboard

    @Test
    void theDashboardCarriesTheUnitsAndTheLockStateOfEveryTile() throws Exception {
        mockMvc.perform(signedIn(get("/api/dashboard").param("level", "A1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.course.name").value("German A1"))
                .andExpect(jsonPath("$.units.length()").value(1))
                .andExpect(jsonPath("$.units[0].name").value("Basics"))
                .andExpect(jsonPath("$.units[0].lessons.length()").value(2))
                .andExpect(jsonPath("$.units[0].lessons[0].name").value("Greetings"))
                .andExpect(jsonPath("$.units[0].lessons[0].completed").value(true))
                .andExpect(jsonPath("$.units[0].lessons[0].unlocked").value(true))
                .andExpect(jsonPath("$.units[0].lessons[1].name").value("Introducing Yourself"))
                .andExpect(jsonPath("$.units[0].lessons[1].completed").value(false))
                .andExpect(jsonPath("$.units[0].lessons[1].unlocked").value(true));
    }

    @Test
    void theCourseApiNeedsASession() throws Exception {
        mockMvc.perform(get("/api/levels"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isUnauthorized());
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder signedIn(
            org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder request) {
        return request.with(user("anna"));
    }
}
