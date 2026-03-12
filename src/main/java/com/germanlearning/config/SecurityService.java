package com.germanlearning.config;

import com.germanlearning.model.User;
import com.germanlearning.repository.UserRepository;
import com.vaadin.flow.spring.security.AuthenticationContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class SecurityService {

    private final AuthenticationContext authenticationContext;
    private final UserRepository userRepository;

    public SecurityService(AuthenticationContext authenticationContext, UserRepository userRepository) {
        this.authenticationContext = authenticationContext;
        this.userRepository = userRepository;
    }

    public Optional<UserDetails> getAuthenticatedUser() {
        return authenticationContext.getAuthenticatedUser(UserDetails.class);
    }

    public Optional<User> getCurrentUser() {
        return getAuthenticatedUser()
                .flatMap(ud -> userRepository.findByUsername(ud.getUsername()));
    }

    public String getCurrentUsername() {
        return getAuthenticatedUser()
                .map(UserDetails::getUsername)
                .orElse("");
    }

    public boolean isAuthenticated() {
        return getAuthenticatedUser().isPresent();
    }

    public void logout() {
        authenticationContext.logout();
    }
}
