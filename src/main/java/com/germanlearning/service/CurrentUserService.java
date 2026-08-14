package com.germanlearning.service;

import com.germanlearning.model.User;
import com.germanlearning.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/** Resolves the logged in user from the Spring Security context. */
@Service
@Transactional(readOnly = true)
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Optional<User> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return Optional.empty();
        }
        return userRepository.findByUsername(authentication.getName());
    }

    /** For endpoints behind authentication, where a missing user is a bug. */
    public User requireCurrentUser() {
        return getCurrentUser()
                .orElseThrow(() -> new IllegalStateException("No authenticated user"));
    }
}
