package com.germanlearning.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.germanlearning.config.SecurityConfig;
import com.germanlearning.model.User;
import com.germanlearning.service.AuthService;
import com.germanlearning.service.CurrentUserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The HTTP contract of the auth endpoints: which bodies are refused before a
 * user is ever created, what a successful registration returns, and how a
 * failed login and an anonymous /me are reported.
 *
 * The real {@link SecurityConfig} is imported rather than the slice default, so
 * the CSRF rules the SPA has to satisfy are part of what is being tested.
 */
@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Test
    void anEmptyFieldIsRejected() throws Exception {
        postJson("/api/auth/register", Map.of("username", "", "email", "", "password", ""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Please fill in all fields"));

        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void aUsernameShorterThanThreeCharactersIsRejected() throws Exception {
        postJson("/api/auth/register",
                Map.of("username", "an", "email", "anna@example.com", "password", "secret123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username must be at least 3 characters"));

        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void aPasswordShorterThanSixCharactersIsRejected() throws Exception {
        postJson("/api/auth/register",
                Map.of("username", "anna", "email", "anna@example.com", "password", "12345"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must be at least 6 characters"));

        verify(authService, never()).register(any(), any(), any());
    }

    @Test
    void aValidRegistrationReturnsCreatedAndTheNewUser() throws Exception {
        when(authService.register("anna", "anna@example.com", "secret123"))
                .thenReturn(user("anna", "anna@example.com"));

        postJson("/api/auth/register",
                Map.of("username", "anna", "email", "anna@example.com", "password", "secret123"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.username").value("anna"))
                .andExpect(jsonPath("$.email").value("anna@example.com"))
                .andExpect(jsonPath("$.totalXp").value(0))
                // Whatever else the record grows, the hash is never part of it
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void aTakenUsernameComesBackAsABadRequest() throws Exception {
        when(authService.register(any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        postJson("/api/auth/register",
                Map.of("username", "anna", "email", "anna@example.com", "password", "secret123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Username already exists"));
    }

    @Test
    void loginWithWrongCredentialsIsUnauthorized() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        postJson("/api/auth/login", Map.of("username", "anna", "password", "wrong"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void theCurrentUserEndpointIsUnauthorizedWithoutASession() throws Exception {
        when(currentUserService.getCurrentUser()).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }

    private org.springframework.test.web.servlet.ResultActions postJson(String path, Map<String, String> body)
            throws Exception {
        return mockMvc.perform(post(path)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    private User user(String username, String email) {
        User user = new User(username, email, "$2a$10$hash");
        user.setId(7L);
        return user;
    }
}
