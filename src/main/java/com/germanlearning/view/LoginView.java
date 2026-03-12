package com.germanlearning.view;

import com.germanlearning.service.AuthService;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route("login")
@PageTitle("Login | German Learning")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm loginForm = new LoginForm();

    public LoginView(AuthService authService) {
        addClassName("login-view");
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

        H1 title = new H1("German Learning");
        title.getStyle()
                .set("color", "#58CC02")
                .set("margin-bottom", "10px");

        Paragraph subtitle = new Paragraph("Learn German the fun way!");
        subtitle.getStyle()
                .set("color", "#777")
                .set("margin-bottom", "20px");

        loginForm.setAction("login");
        loginForm.setForgotPasswordButtonVisible(false);

        RouterLink registerLink = new RouterLink("Don't have an account? Register here", RegisterView.class);
        registerLink.getStyle()
                .set("color", "#1CB0F6")
                .set("margin-top", "20px");

        card.add(title, subtitle, loginForm, registerLink);
        add(card);
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (event.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            loginForm.setError(true);
        }
    }
}
