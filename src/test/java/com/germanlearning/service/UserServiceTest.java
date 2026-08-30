package com.germanlearning.service;

import com.germanlearning.model.User;
import com.germanlearning.repository.ProgressRepository;
import com.germanlearning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The daily streak, which is the number the profile page reports as "N days"
 * and the flame in the navbar counts.
 *
 * The awkward case is the first day. A new account is created with
 * lastActiveAt already set to now, so "today equals the last active day" is
 * true from the very first lesson, and an implementation that only handles the
 * null case leaves a learner on zero until tomorrow.
 */
class UserServiceTest {

    private static final Long USER_ID = 1L;

    private UserRepository userRepository;
    private UserService userService;
    private User user;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        ProgressRepository progressRepository = mock(ProgressRepository.class);
        userService = new UserService(userRepository, progressRepository);

        user = new User("anna", "anna@example.com", "hash");
        user.setId(USER_ID);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void theFirstDayCountsAsOne() {
        // A brand new account: the constructor already stamped lastActiveAt
        assertEquals(0, user.getCurrentStreak());

        assertEquals(1, userService.updateStreak(USER_ID).getCurrentStreak());
    }

    @Test
    void comingBackTwiceInADayDoesNotCountTwice() {
        userService.updateStreak(USER_ID);

        assertEquals(1, userService.updateStreak(USER_ID).getCurrentStreak());
    }

    @Test
    void aDayAfterTheLastOneExtendsTheStreak() {
        user.setCurrentStreak(4);
        user.setLastActiveAt(LocalDateTime.now().minusDays(1));

        assertEquals(5, userService.updateStreak(USER_ID).getCurrentStreak());
    }

    @Test
    void missingADayStartsOverAtOne() {
        user.setCurrentStreak(9);
        user.setLastActiveAt(LocalDateTime.now().minusDays(2));

        assertEquals(1, userService.updateStreak(USER_ID).getCurrentStreak());
    }

    @Test
    void anAccountThatWasNeverActiveStartsAtOne() {
        user.setLastActiveAt(null);

        assertEquals(1, userService.updateStreak(USER_ID).getCurrentStreak());
    }

    @Test
    void theLongestStreakRemembersTheBestRunEvenAfterItBreaks() {
        user.setCurrentStreak(6);
        user.setLastActiveAt(LocalDateTime.now().minusDays(1));
        userService.updateStreak(USER_ID);
        assertEquals(7, user.getLongestStreak());

        user.setLastActiveAt(LocalDateTime.now().minusDays(3));
        userService.updateStreak(USER_ID);

        assertEquals(1, user.getCurrentStreak());
        assertEquals(7, user.getLongestStreak());
    }

    @Test
    void theLastActiveDayIsMovedForwardEveryTime() {
        user.setLastActiveAt(LocalDateTime.now().minusDays(1));

        userService.updateStreak(USER_ID);

        assertEquals(LocalDateTime.now().toLocalDate(), user.getLastActiveAt().toLocalDate());
    }

    @Test
    void anUnknownUserIsRejected() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> userService.updateStreak(99L));

        assertEquals("User not found", failure.getMessage());
    }
}
