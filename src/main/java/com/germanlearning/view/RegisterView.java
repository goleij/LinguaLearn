package com.germanlearning.view;

import com.germanlearning.service.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.data.validator.StringLengthValidator;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("register")
@PageTitle("Register | German Learning")
@AnonymousAllowed
public class RegisterView extends VerticalLayout {

    private final AuthService authService;

    public RegisterView(AuthService authService) {
        this.authService = authService;

        addClassName("register-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        getStyle()
                .set("background", "linear-gradient(135deg, #58CC02 0%, #1CB0F6 100%)")
                .set("padding", "20px");

        VerticalLayout card = new VerticalLayout();
        card.setWidth("400px");
        card.setAlignItems(Alignment.CENTER);
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "20px")
                .set("padding", "40px")
                .set("box-shadow", "0 4px 20px rgba(0,0,0,0.1)");

        H1 title = new H1("Create Account");
        title.getStyle()
                .set("color", "#58CC02")
                .set("margin-bottom", "10px");

        Paragraph subtitle = new Paragraph("Start your German learning journey!");
        subtitle.getStyle()
                .set("color", "#777")
                .set("margin-bottom", "20px");

        TextField usernameField = new TextField("Username");
        usernameField.setWidthFull();
        usernameField.setPlaceholder("Choose a username");
        styleField(usernameField);

        EmailField emailField = new EmailField("Email");
        emailField.setWidthFull();
        emailField.setPlaceholder("your@email.com");
        styleField(emailField);

        PasswordField passwordField = new PasswordField("Password");
        passwordField.setWidthFull();
        passwordField.setPlaceholder("At least 6 characters");
        styleField(passwordField);

        PasswordField confirmPasswordField = new PasswordField("Confirm Password");
        confirmPasswordField.setWidthFull();
        confirmPasswordField.setPlaceholder("Repeat your password");
        styleField(confirmPasswordField);

        Button registerButton = new Button("Create Account");
        registerButton.setWidthFull();
        registerButton.getStyle()
                .set("background-color", "#58CC02")
                .set("color", "white")
                .set("border-radius", "12px")
                .set("padding", "15px")
                .set("font-weight", "bold")
                .set("font-size", "16px")
                .set("cursor", "pointer")
                .set("border", "none");

        registerButton.addClickListener(e -> {
            String username = usernameField.getValue().trim();
            String email = emailField.getValue().trim();
            String password = passwordField.getValue();
            String confirmPassword = confirmPasswordField.getValue();

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showError("Please fill in all fields");
                return;
            }

            if (username.length() < 3) {
                showError("Username must be at least 3 characters");
                return;
            }

            if (password.length() < 6) {
                showError("Password must be at least 6 characters");
                return;
            }

            if (!password.equals(confirmPassword)) {
                showError("Passwords do not match");
                return;
            }

            try {
                authService.register(username, email, password);
                showSuccess("Account created successfully! Please login.");
                UI.getCurrent().navigate("login");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            }
        });

        RouterLink loginLink = new RouterLink("Already have an account? Login here", LoginView.class);
        loginLink.getStyle()
                .set("color", "#1CB0F6")
                .set("margin-top", "20px");

        card.add(title, subtitle, usernameField, emailField, passwordField, confirmPasswordField, registerButton, loginLink);
        add(card);
    }

    private void styleField(com.vaadin.flow.component.Component field) {
        field.getElement().getStyle()
                .set("--vaadin-input-field-border-radius", "12px");
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
