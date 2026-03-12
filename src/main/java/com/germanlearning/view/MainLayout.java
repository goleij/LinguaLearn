package com.germanlearning.view;

import com.germanlearning.config.SecurityService;
import com.germanlearning.model.User;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.theme.lumo.LumoUtility;

public class MainLayout extends AppLayout {

    private final SecurityService securityService;

    public MainLayout(SecurityService securityService) {
        this.securityService = securityService;
        createHeader();
        createDrawer();
    }

    private void createHeader() {
        H1 logo = new H1("German Learning");
        logo.addClassNames(
                LumoUtility.FontSize.LARGE,
                LumoUtility.Margin.MEDIUM);
        logo.getStyle().set("color", "#58CC02");

        HorizontalLayout header = new HorizontalLayout();
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames(
                LumoUtility.Padding.Vertical.NONE,
                LumoUtility.Padding.Horizontal.MEDIUM);

        header.add(new DrawerToggle(), logo);

        securityService.getCurrentUser().ifPresent(user -> {
            Span xpBadge = new Span("XP: " + user.getTotalXp());
            xpBadge.getStyle()
                    .set("background-color", "#FFC800")
                    .set("color", "#3C3C3C")
                    .set("padding", "5px 15px")
                    .set("border-radius", "20px")
                    .set("font-weight", "bold");

            Span streakBadge = new Span("🔥 " + user.getCurrentStreak());
            streakBadge.getStyle()
                    .set("background-color", "#FF9600")
                    .set("color", "white")
                    .set("padding", "5px 15px")
                    .set("border-radius", "20px")
                    .set("font-weight", "bold");

            Button logoutButton = new Button("Logout", e -> securityService.logout());
            logoutButton.getStyle()
                    .set("background-color", "#E5E5E5")
                    .set("color", "#3C3C3C")
                    .set("border-radius", "12px");

            HorizontalLayout rightSection = new HorizontalLayout(xpBadge, streakBadge, logoutButton);
            rightSection.setSpacing(true);
            rightSection.setAlignItems(FlexComponent.Alignment.CENTER);

            header.expand(logo);
            header.add(rightSection);
        });

        addToNavbar(header);
    }

    private void createDrawer() {
        VerticalLayout drawer = new VerticalLayout();
        drawer.setPadding(true);
        drawer.setSpacing(true);

        H2 menuTitle = new H2("Menu");
        menuTitle.getStyle().set("color", "#3C3C3C");

        RouterLink dashboardLink = new RouterLink("Dashboard", DashboardView.class);
        styleNavLink(dashboardLink);

        RouterLink profileLink = new RouterLink("Profile", ProfileView.class);
        styleNavLink(profileLink);

        drawer.add(menuTitle, dashboardLink, profileLink);
        addToDrawer(drawer);
    }

    private void styleNavLink(RouterLink link) {
        link.getStyle()
                .set("display", "block")
                .set("padding", "10px 15px")
                .set("text-decoration", "none")
                .set("color", "#3C3C3C")
                .set("border-radius", "10px")
                .set("font-weight", "500");
    }
}
