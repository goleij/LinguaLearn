package com.germanlearning.view;

import com.germanlearning.config.SecurityService;
import com.germanlearning.model.Unit;
import com.germanlearning.model.User;
import com.germanlearning.service.LessonService;
import com.germanlearning.service.LessonService.LessonStatus;
import com.vaadin.flow.component.UI;
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
import java.util.List;

@Route(value = "", layout = MainLayout.class)
@PageTitle("Dashboard | German Learning")
@PermitAll
public class DashboardView extends VerticalLayout {

    private final LessonService lessonService;
    private final SecurityService securityService;

    public DashboardView(LessonService lessonService, SecurityService securityService) {
        this.lessonService = lessonService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);
        getStyle().set("background-color", "#F7F7F7");

        securityService.getCurrentUser().ifPresent(this::buildDashboard);
    }

    private void buildDashboard(User user) {
        H2 welcomeTitle = new H2("Welcome back, " + user.getUsername() + "!");
        welcomeTitle.getStyle().set("color", "#3C3C3C");

        Paragraph intro = new Paragraph("Continue your German learning journey");
        intro.getStyle().set("color", "#777");

        add(welcomeTitle, intro);

        lessonService.getAllCourses().stream().findFirst().ifPresent(course -> {
            List<Unit> units = lessonService.getUnitsForCourse(course.getId());
            List<LessonStatus> allLessons = lessonService.getLessonsWithStatus(user.getId(), course.getId());

            for (Unit unit : units) {
                add(createUnitCard(unit, allLessons, user.getId()));
            }
        });
    }

    private VerticalLayout createUnitCard(Unit unit, List<LessonStatus> allLessons, Long userId) {
        VerticalLayout unitCard = new VerticalLayout();
        unitCard.setWidthFull();
        unitCard.setPadding(true);
        unitCard.setSpacing(true);
        unitCard.getStyle()
                .set("background-color", "white")
                .set("border-radius", "16px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.05)")
                .set("margin-bottom", "20px");

        H3 unitTitle = new H3(unit.getName());
        unitTitle.getStyle().set("color", "#58CC02");

        if (unit.getDescription() != null && !unit.getDescription().isEmpty()) {
            Paragraph desc = new Paragraph(unit.getDescription());
            desc.getStyle().set("color", "#777");
            unitCard.add(unitTitle, desc);
        } else {
            unitCard.add(unitTitle);
        }

        HorizontalLayout lessonsRow = new HorizontalLayout();
        lessonsRow.setWidthFull();
        lessonsRow.setSpacing(true);
        lessonsRow.setAlignItems(FlexComponent.Alignment.CENTER);
        lessonsRow.getStyle().set("flex-wrap", "wrap");

        for (LessonStatus status : allLessons) {
            if (status.getLesson().getUnit().getId().equals(unit.getId())) {
                lessonsRow.add(createLessonButton(status));
            }
        }

        unitCard.add(lessonsRow);
        return unitCard;
    }

    private Div createLessonButton(LessonStatus status) {
        Div lessonBtn = new Div();
        lessonBtn.setWidth("120px");
        lessonBtn.setHeight("120px");
        lessonBtn.getStyle()
                .set("border-radius", "16px")
                .set("display", "flex")
                .set("flex-direction", "column")
                .set("align-items", "center")
                .set("justify-content", "center")
                .set("cursor", status.isUnlocked() ? "pointer" : "not-allowed")
                .set("transition", "transform 0.2s");

        if (status.isCompleted()) {
            lessonBtn.getStyle()
                    .set("background-color", "#58CC02")
                    .set("color", "white");
        } else if (status.isUnlocked()) {
            lessonBtn.getStyle()
                    .set("background-color", "#1CB0F6")
                    .set("color", "white");
        } else {
            lessonBtn.getStyle()
                    .set("background-color", "#E5E5E5")
                    .set("color", "#AFAFAF");
        }

        Span icon = new Span(status.isCompleted() ? "✓" : status.isUnlocked() ? "▶" : "🔒");
        icon.getStyle().set("font-size", "24px");

        Span name = new Span(status.getLesson().getName());
        name.getStyle()
                .set("font-size", "12px")
                .set("font-weight", "bold")
                .set("text-align", "center")
                .set("margin-top", "8px");

        lessonBtn.add(icon, name);

        if (status.isUnlocked()) {
            lessonBtn.addClickListener(e -> {
                UI.getCurrent().navigate("lesson/" + status.getLesson().getId());
            });
            lessonBtn.getElement().addEventListener("mouseover", e -> {})
                    .addEventData("element.style.transform='scale(1.05)'");
            lessonBtn.getElement().addEventListener("mouseout", e -> {})
                    .addEventData("element.style.transform='scale(1)'");
        }

        return lessonBtn;
    }
}
