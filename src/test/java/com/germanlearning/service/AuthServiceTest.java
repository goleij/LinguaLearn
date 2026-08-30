package com.germanlearning.service;

import com.germanlearning.model.User;
import com.germanlearning.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * The account rules: a username and an email may each be taken once, the plain
 * password never reaches the database, and a login only succeeds for the right
 * user with the right password.
 *
 * The encoder is the real BCrypt one rather than a mock, because "the password
 * is hashed" is exactly the property worth checking and a stubbed encoder would
 * check nothing.
 */
class AuthServiceTest {

    private static final String USERNAME = "anna";
    private static final String EMAIL = "anna@example.com";
    private static final String PASSWORD = "secret123";

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, passwordEncoder);

        when(userRepository.existsByUsername(any())).thenReturn(false);
        when(userRepository.existsByEmail(any())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(call -> call.getArgument(0));
    }

    @Test
    void registeringATakenUsernameIsRejected() {
        when(userRepository.existsByUsername(USERNAME)).thenReturn(true);

        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> authService.register(USERNAME, EMAIL, PASSWORD));

        assertEquals("Username already exists", failure.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void registeringATakenEmailIsRejected() {
        when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

        IllegalArgumentException failure = assertThrows(IllegalArgumentException.class,
                () -> authService.register(USERNAME, EMAIL, PASSWORD));

        assertEquals("Email already registered", failure.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void theStoredPasswordIsABcryptHashAndNotThePlainText() {
        User registered = authService.register(USERNAME, EMAIL, PASSWORD);

        assertNotEquals(PASSWORD, registered.getPasswordHash());
        assertTrue(registered.getPasswordHash().startsWith("$2"), "expected a BCrypt hash");
        assertTrue(passwordEncoder.matches(PASSWORD, registered.getPasswordHash()));
    }

    @Test
    void authenticateRejectsAWrongPassword() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser()));

        assertTrue(authService.authenticate(USERNAME, "wrong-password").isEmpty());
    }

    @Test
    void authenticateRejectsAnUnknownUsername() {
        when(userRepository.findByUsername("nobody")).thenReturn(Optional.empty());

        assertTrue(authService.authenticate("nobody", PASSWORD).isEmpty());
    }

    @Test
    void authenticateReturnsTheUserForTheRightPassword() {
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser()));

        Optional<User> authenticated = authService.authenticate(USERNAME, PASSWORD);

        assertTrue(authenticated.isPresent());
        assertEquals(USERNAME, authenticated.get().getUsername());
    }

    private User existingUser() {
        User user = new User(USERNAME, EMAIL, passwordEncoder.encode(PASSWORD));
        user.setId(1L);
        return user;
    }
}
