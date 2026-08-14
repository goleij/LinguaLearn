package com.germanlearning.controller;

import com.germanlearning.dto.ErrorResponse;
import com.germanlearning.dto.LoginRequest;
import com.germanlearning.dto.RegisterRequest;
import com.germanlearning.dto.UserDto;
import com.germanlearning.model.User;
import com.germanlearning.service.AuthService;
import com.germanlearning.service.CurrentUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Session based authentication for the React client.
 *
 * Same rules as before: BCrypt hashes via AuthService, credentials checked by
 * Spring Security's AuthenticationManager, and the authenticated context stored
 * in the HTTP session (JSESSIONID cookie).
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CurrentUserService currentUserService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository =
            new HttpSessionSecurityContextRepository();

    public AuthController(AuthService authService,
            CurrentUserService currentUserService,
            AuthenticationManager authenticationManager) {
        this.authService = authService;
        this.currentUserService = currentUserService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        String username = request.username() == null ? "" : request.username().trim();
        String email = request.email() == null ? "" : request.email().trim();
        String password = request.password() == null ? "" : request.password();

        // Mirrors the client side validation so the rules also hold when the
        // endpoint is called directly
        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            return badRequest("Please fill in all fields");
        }
        if (username.length() < 3) {
            return badRequest("Username must be at least 3 characters");
        }
        if (password.length() < 6) {
            return badRequest("Password must be at least 6 characters");
        }

        User user = authService.register(username, email, password);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserDto.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(
                            request.username(), request.password()));

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            // Session fixation protection: a new id for the authenticated session
            if (servletRequest.getSession(false) != null) {
                servletRequest.changeSessionId();
            }
            securityContextRepository.saveContext(context, servletRequest, servletResponse);

            return ResponseEntity.ok(UserDto.from(currentUserService.requireCurrentUser()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(
                            "Check that you have entered the correct username and password and try again."));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest servletRequest) {
        HttpSession session = servletRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    /** Returns the signed in user, or 401 when there is none. */
    @GetMapping("/me")
    public ResponseEntity<?> currentUser() {
        return currentUserService.getCurrentUser()
                .map(user -> ResponseEntity.ok((Object) UserDto.from(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse("Not authenticated")));
    }

    private ResponseEntity<ErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }
}
