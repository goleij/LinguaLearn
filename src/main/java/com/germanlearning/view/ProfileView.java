package com.germanlearning.view;

import com.germanlearning.config.SecurityService;
import com.germanlearning.model.Progress;
import com.germanlearning.model.User;
import com.germanlearning.service.ProgressService;
import com.germanlearning.service.UserService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Route(value = "profile", layout = MainLayout.class)
@PageTitle("Profile | German Learning")
@PermitAll
public class ProfileView extends VerticalLayout {

        private final SecurityService securityService;
        private final UserService userService;
        private final ProgressService progressService;

        public ProfileView(SecurityService securityService,
                        UserService userService,
                        ProgressService progressService) {
                this.securityService = securityService;
                this.userService = userService;
                this.progressService = progressService;

                setSizeFull();
                setPadding(true);
                setAlignItems(FlexComponent.Alignment.CENTER);
                getStyle().set("background-color", "#F7F7F7");

                securityService.getCurrentUser().ifPresent(this::buildProfile);
        }

        private void buildProfile(User user) {
                VerticalLayout mainCard = new VerticalLayout();
                mainCard.setWidth("100%");
                mainCard.setMaxWidth("600px");
                mainCard.setPadding(true);
                mainCard.setSpacing(true);
                mainCard.setAlignItems(FlexComponent.Alignment.CENTER);
                mainCard.getStyle()
                                .set("background-color", "white")
                                .set("border-radius", "20px")
                                .set("box-shadow", "0 4px 20px rgba(0,0,0,0.1)");

                Div avatar = new Div();
                avatar.getStyle()
                                .set("width", "100px")
                                .set("height", "100px")
                                .set("border-radius", "50%")
                                .set("background-color", "#58CC02")
                                .set("display", "flex")
                                .set("align-items", "center")
                                .set("justify-content", "center");

                Span avatarText = new Span(user.getUsername().substring(0, 1).toUpperCase());
                avatarText.getStyle()
                                .set("color", "white")
                                .set("font-size", "40px")
                                .set("font-weight", "bold");
                avatar.add(avatarText);

                H2 username = new H2(user.getUsername());
                username.getStyle().set("color", "#3C3C3C").set("margin", "10px 0 5px 0");

                Paragraph email = new Paragraph(user.getEmail());
                email.getStyle().set("color", "#777").set("margin", "0");

                HorizontalLayout statsRow = new HorizontalLayout();
                statsRow.setWidthFull();
                statsRow.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
                statsRow.setSpacing(true);
                statsRow.getStyle().set("margin-top", "20px");

                statsRow.add(
                                createStatCard("Total XP", String.valueOf(user.getTotalXp()), "#FFC800"),
                                createStatCard("Streak", user.getCurrentStreak() + " days", "#FF9600"),
                                createStatCard("Completed",
                                                userService.getCompletedLessonsCount(user.getId()) + " lessons",
                                                "#58CC02"));

                mainCard.add(avatar, username, email, statsRow);

                VerticalLayout historySection = new VerticalLayout();
                historySection.setWidthFull();
                historySection.setPadding(false);
                historySection.getStyle().set("margin-top", "30px");

                H3 historyTitle = new H3("Recent Activity");
                historyTitle.getStyle().set("color", "#3C3C3C");
                historySection.add(historyTitle);

                List<Progress> completedLessons = progressService.getCompletedLessons(user.getId());

                if (completedLessons.isEmpty()) {
                        Paragraph noActivity = new Paragraph("No completed lessons yet. Start learning!");
                        noActivity.getStyle().set("color", "#777");
                        historySection.add(noActivity);
                } else {
                        for (Progress progress : completedLessons.stream().limit(5).toList()) {
                                historySection.add(createActivityItem(progress));
                        }
                }

                mainCard.add(historySection);

                String memberSince = user.getCreatedAt().format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                Paragraph memberInfo = new Paragraph("Member since " + memberSince);
                memberInfo.getStyle()
                                .set("color", "#AFAFAF")
                                .set("font-size", "12px")
                                .set("margin-top", "20px");
                mainCard.add(memberInfo);

                add(mainCard);
        }

        private VerticalLayout createStatCard(String label, String value, String color) {
                VerticalLayout card = new VerticalLayout();
                card.setAlignItems(FlexComponent.Alignment.CENTER);
                card.setPadding(true);
                card.setWidth("120px");
                card.getStyle()
                                .set("background-color", color)
                                .set("border-radius", "16px");

                Span valueSpan = new Span(value);
                valueSpan.getStyle()
                                .set("font-size", "24px")
                                .set("font-weight", "bold")
                                .set("color", "white");

                Span labelSpan = new Span(label);
                labelSpan.getStyle()
                                .set("font-size", "12px")
                                .set("color", "rgba(255,255,255,0.8)");

                card.add(valueSpan, labelSpan);
                return card;
        }

        private HorizontalLayout createActivityItem(Progress progress) {
                HorizontalLayout item = new HorizontalLayout();
                item.setWidthFull();
                item.setAlignItems(FlexComponent.Alignment.CENTER);
                item.setPadding(true);
                item.getStyle()
                                .set("background-color", "#F7F7F7")
                                .set("border-radius", "10px")
                                .set("margin-bottom", "10px");

                Span checkmark = new Span("✓");
                checkmark.getStyle()
                                .set("color", "#58CC02")
                                .set("font-size", "20px")
                                .set("margin-right", "10px");

                VerticalLayout details = new VerticalLayout();
                details.setPadding(false);
                details.setSpacing(false);

                Span lessonName = new Span(progress.getLesson().getName());
                lessonName.getStyle().set("font-weight", "bold").set("color", "#3C3C3C");

                String dateStr = progress.getCompletedAt() != null
                                ? progress.getCompletedAt().format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
                                : "Recently";
                Span date = new Span(dateStr);
                date.getStyle().set("color", "#777").set("font-size", "12px");

                details.add(lessonName, date);

                Span xpBadge = new Span("+" + progress.getXpEarned() + " XP");
                xpBadge.getStyle()
                                .set("background-color", "#FFC800")
                                .set("color", "#3C3C3C")
                                .set("padding", "3px 10px")
                                .set("border-radius", "10px")
                                .set("font-size", "12px")
                                .set("font-weight", "bold");

                item.add(checkmark, details, xpBadge);
                item.expand(details);

                return item;
        }
}
