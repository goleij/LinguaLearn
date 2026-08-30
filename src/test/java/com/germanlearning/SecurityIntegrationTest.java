package com.germanlearning;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The security rules as the browser meets them, with the whole application
 * running: the session, the CSRF filter and the JSON 401 entry point together.
 *
 * The "test" profile points the datasource at a throwaway file under target/,
 * so registering users here never touches the real germanlearning.db.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityIntegrationTest {

    private static final String PASSWORD = "secret123";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void anApiCallWithoutASessionIsUnauthorizedWithAJsonBody() throws Exception {
        mockMvc.perform(get("/api/courses"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.message").value("Not authenticated"));
    }

    @Test
    void registeringIsAllowedWithoutASession() throws Exception {
        String username = uniqueName();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "email", username + "@example.com",
                                "password", PASSWORD))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(username));
    }

    @Test
    void aPostWithoutACsrfTokenIsForbidden() throws Exception {
        String username = uniqueName();

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "email", username + "@example.com",
                                "password", PASSWORD))))
                .andExpect(status().isForbidden());
    }

    @Test
    void aLoggedInSessionCanReadTheApi() throws Exception {
        MockHttpSession session = registerAndLogIn();

        mockMvc.perform(get("/api/courses").session(session))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").exists());
    }

    @Test
    void afterLoggingOutTheSessionNoLongerIdentifiesTheUser() throws Exception {
        MockHttpSession session = registerAndLogIn();

        mockMvc.perform(post("/api/auth/logout").with(csrf()).session(session))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/auth/me").session(session))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Not authenticated"));

        mockMvc.perform(get("/api/courses").session(session))
                .andExpect(status().isUnauthorized());
    }

    /** Registers a fresh account and returns the session the login produced. */
    private MockHttpSession registerAndLogIn() throws Exception {
        String username = uniqueName();

        mockMvc.perform(post("/api/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "username", username,
                                "email", username + "@example.com",
                                "password", PASSWORD))))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/auth/login")
                        .with(csrf())
                        .session(new MockHttpSession())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", PASSWORD))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(username))
                .andReturn();

        MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(session, "the login should have created a session");
        return session;
    }

    /** Every test brings its own account, so order and reruns do not matter. */
    private String uniqueName() {
        return "user" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private String json(Map<String, String> body) throws Exception {
        return objectMapper.writeValueAsString(body);
    }
}
