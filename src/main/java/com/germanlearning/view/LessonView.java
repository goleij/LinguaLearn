package com.germanlearning.view;

import com.germanlearning.config.SecurityService;
import com.germanlearning.model.*;
import com.germanlearning.service.ExerciseService;
import com.germanlearning.service.ExerciseService.ExerciseResult;
import com.germanlearning.service.LessonService;
import com.germanlearning.service.ProgressService;
import com.germanlearning.view.components.*;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.BeforeEvent;
import com.vaadin.flow.router.HasUrlParameter;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;
import java.util.List;

@Route(value = "lesson", layout = MainLayout.class)
@PageTitle("Lesson | German Learning")
@PermitAll
public class LessonView extends VerticalLayout implements HasUrlParameter<Long> {

    private final LessonService lessonService;
    private final ExerciseService exerciseService;
    private final ProgressService progressService;
    private final SecurityService securityService;

    private Long lessonId;
    private Long userId;
    private List<Exercise> exercises;
    private int currentExerciseIndex = 0;
    private int correctAnswers = 0;
    private int totalXpEarned = 0;
    private boolean isPracticeMode = false;

    private ProgressBar progressBar;
    private Span progressLabel;
    private VerticalLayout exerciseContainer;
    private ExerciseComponent currentComponent;

    public LessonView(LessonService lessonService,
            ExerciseService exerciseService,
            ProgressService progressService,
            SecurityService securityService) {
        this.lessonService = lessonService;
        this.exerciseService = exerciseService;
        this.progressService = progressService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);
        setAlignItems(FlexComponent.Alignment.CENTER);
        getStyle().set("background-color", "#F7F7F7");
    }

    @Override
    public void setParameter(BeforeEvent event, Long parameter) {
        this.lessonId = parameter;

        securityService.getCurrentUser().ifPresentOrElse(
                user -> {
                    this.userId = user.getId();
                    loadLesson();
                },
                () -> UI.getCurrent().navigate("login"));
    }

    private void loadLesson() {
        lessonService.getLessonById(lessonId).ifPresentOrElse(
                lesson -> {
                    if (!lessonService.isLessonUnlocked(userId, lessonId)) {
                        Notification.show("This lesson is locked!", 3000, Notification.Position.MIDDLE);
                        UI.getCurrent().navigate("");
                        return;
                    }

                    // Check if lesson was already completed (practice mode)
                    isPracticeMode = progressService.isLessonCompleted(userId, lessonId);

                    exercises = lessonService.getExercisesForLesson(lessonId);
                    if (exercises.isEmpty()) {
                        showNoExercisesMessage();
                        return;
                    }

                    buildLessonUI(lesson);
                },
                () -> {
                    Notification.show("Lesson not found", 3000, Notification.Position.MIDDLE);
                    UI.getCurrent().navigate("");
                });
    }

    private void buildLessonUI(Lesson lesson) {
        removeAll();

        VerticalLayout mainCard = new VerticalLayout();
        mainCard.setWidth("100%");
        mainCard.setMaxWidth("600px");
        mainCard.setPadding(true);
        mainCard.setSpacing(true);
        mainCard.getStyle()
                .set("background-color", "white")
                .set("border-radius", "20px")
                .set("box-shadow", "0 4px 20px rgba(0,0,0,0.1)");

        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.CENTER);

        Button backButton = new Button("← Back");
        backButton.getStyle()
                .set("background-color", "#E5E5E5")
                .set("color", "#3C3C3C")
                .set("border-radius", "10px");
        backButton.addClickListener(e -> UI.getCurrent().navigate(""));

        H2 title = new H2(lesson.getName());
        title.getStyle().set("color", "#58CC02");

        header.add(backButton, title);
        header.expand(title);

        HorizontalLayout progressRow = new HorizontalLayout();
        progressRow.setWidthFull();
        progressRow.setAlignItems(FlexComponent.Alignment.CENTER);

        progressBar = new ProgressBar();
        progressBar.setWidthFull();
        progressBar.setValue(0);
        progressBar.getStyle().set("--vaadin-progress-bar-color", "#58CC02");

        progressLabel = new Span("0 / " + exercises.size());
        progressLabel.getStyle().set("color", "#777").set("min-width", "60px");

        progressRow.add(progressBar, progressLabel);
        progressRow.expand(progressBar);

        exerciseContainer = new VerticalLayout();
        exerciseContainer.setWidthFull();
        exerciseContainer.setPadding(false);

        mainCard.add(header, progressRow, exerciseContainer);
        add(mainCard);

        showCurrentExercise();
    }

    private void showCurrentExercise() {
        exerciseContainer.removeAll();

        if (currentExerciseIndex >= exercises.size()) {
            showCompletionScreen();
            return;
        }

        Exercise exercise = exercises.get(currentExerciseIndex);

        currentComponent = createExerciseComponent(exercise);
        currentComponent.setAnswerHandler(this::handleAnswer);

        exerciseContainer.add(currentComponent);
        updateProgress();
    }

    private ExerciseComponent createExerciseComponent(Exercise exercise) {
        if (exercise instanceof MultipleChoiceExercise) {
            return new MultipleChoiceComponent((MultipleChoiceExercise) exercise);
        } else if (exercise instanceof FillBlankExercise) {
            return new FillBlankComponent((FillBlankExercise) exercise);
        } else if (exercise instanceof MatchExercise) {
            return new MatchComponent((MatchExercise) exercise);
        } else if (exercise instanceof OrderExercise) {
            return new OrderComponent((OrderExercise) exercise);
        }

        return new MultipleChoiceComponent((MultipleChoiceExercise) exercise);
    }

    private void handleAnswer(String answer) {
        Exercise exercise = exercises.get(currentExerciseIndex);
        ExerciseResult result = exerciseService.validateAnswer(exercise.getId(), answer);

        progressService.recordAnswer(userId, lessonId, result.isCorrect());

        if (result.isCorrect()) {
            correctAnswers++;
            totalXpEarned += result.getXpEarned();
            showFeedback(true, result);
        } else {
            showFeedback(false, result);
        }
    }

    private void showFeedback(boolean correct, ExerciseResult result) {
        exerciseContainer.removeAll();

        VerticalLayout feedbackCard = new VerticalLayout();
        feedbackCard.setWidthFull();
        feedbackCard.setAlignItems(FlexComponent.Alignment.CENTER);
        feedbackCard.setPadding(true);
        feedbackCard.getStyle()
                .set("background-color", correct ? "#D7FFB8" : "#FFDFE0")
                .set("border-radius", "16px");

        Span icon = new Span(correct ? "✓" : "✗");
        icon.getStyle()
                .set("font-size", "48px")
                .set("color", correct ? "#58CC02" : "#FF4B4B");

        H3 message = new H3(correct ? "Correct!" : "Not quite right");
        message.getStyle().set("color", correct ? "#58CC02" : "#FF4B4B");

        feedbackCard.add(icon, message);

        if (!correct && result.getCorrectAnswer() != null) {
            Paragraph correctAns = new Paragraph("Correct answer: " + result.getCorrectAnswer());
            correctAns.getStyle().set("color", "#3C3C3C").set("font-weight", "bold");
            feedbackCard.add(correctAns);
        }

        if (result.getExplanation() != null && !result.getExplanation().isEmpty()) {
            Paragraph explanation = new Paragraph(result.getExplanation());
            explanation.getStyle().set("color", "#777");
            feedbackCard.add(explanation);
        }

        if (correct) {
            Span xpBadge = new Span("+" + result.getXpEarned() + " XP");
            xpBadge.getStyle()
                    .set("background-color", "#FFC800")
                    .set("color", "#3C3C3C")
                    .set("padding", "5px 15px")
                    .set("border-radius", "20px")
                    .set("font-weight", "bold");
            feedbackCard.add(xpBadge);
        }

        Button continueBtn = new Button("Continue");
        continueBtn.getStyle()
                .set("background-color", correct ? "#58CC02" : "#1CB0F6")
                .set("color", "white")
                .set("border-radius", "12px")
                .set("padding", "15px 40px")
                .set("font-weight", "bold")
                .set("margin-top", "20px");
        continueBtn.addClickListener(e -> {
            currentExerciseIndex++;
            showCurrentExercise();
        });

        feedbackCard.add(continueBtn);
        exerciseContainer.add(feedbackCard);
    }

    private void showCompletionScreen() {
        exerciseContainer.removeAll();

        progressService.completeLesson(userId, lessonId);

        VerticalLayout completionCard = new VerticalLayout();
        completionCard.setWidthFull();
        completionCard.setAlignItems(FlexComponent.Alignment.CENTER);
        completionCard.setPadding(true);
        completionCard.getStyle()
                .set("background-color", "#D7FFB8")
                .set("border-radius", "16px");

        Span trophy = new Span("🏆");
        trophy.getStyle().set("font-size", "64px");

        H2 congrats = new H2("Lesson Complete!");
        congrats.getStyle().set("color", "#58CC02");

        double score = (double) correctAnswers / exercises.size() * 100;
        Paragraph scoreText = new Paragraph(String.format("Score: %d/%d (%.0f%%)",
                correctAnswers, exercises.size(), score));
        scoreText.getStyle().set("font-size", "18px").set("color", "#3C3C3C");

        Span xpBadge = new Span("+" + totalXpEarned + " XP earned!");
        xpBadge.getStyle()
                .set("background-color", "#FFC800")
                .set("color", "#3C3C3C")
                .set("padding", "10px 25px")
                .set("border-radius", "25px")
                .set("font-weight", "bold")
                .set("font-size", "20px");

        completionCard.add(trophy, congrats, scoreText);

        // Show XP badge only if not in practice mode
        if (isPracticeMode) {
            Span practiceLabel = new Span("🔄 Practice Mode - No XP Earned");
            practiceLabel.getStyle()
                    .set("background-color", "#777")
                    .set("color", "white")
                    .set("padding", "10px 25px")
                    .set("border-radius", "25px")
                    .set("font-weight", "bold")
                    .set("font-size", "16px");
            completionCard.add(practiceLabel);
        } else {
            completionCard.add(xpBadge);
        }

        boolean passed = score >= 80;
        Paragraph passMessage = new Paragraph(passed
                ? "Great job! You've unlocked the next lesson!"
                : "You need 80% to pass. Try again!");
        passMessage.getStyle()
                .set("color", passed ? "#58CC02" : "#FF9600")
                .set("font-weight", "bold");

        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);

        Button retryBtn = new Button("Practice Again");
        retryBtn.getStyle()
                .set("background-color", "#1CB0F6")
                .set("color", "white")
                .set("border-radius", "12px")
                .set("padding", "15px 30px");
        retryBtn.addClickListener(e -> {
            currentExerciseIndex = 0;
            correctAnswers = 0;
            totalXpEarned = 0;
            progressService.resetLessonProgressForRetry(userId, lessonId);
            showCurrentExercise();
        });

        Button homeBtn = new Button("Back to Dashboard");
        homeBtn.getStyle()
                .set("background-color", "#58CC02")
                .set("color", "white")
                .set("border-radius", "12px")
                .set("padding", "15px 30px");
        homeBtn.addClickListener(e -> UI.getCurrent().navigate(""));

        buttons.add(retryBtn, homeBtn);

        completionCard.add(trophy, congrats, scoreText, xpBadge, passMessage, buttons);
        exerciseContainer.add(completionCard);
    }

    private void updateProgress() {
        double progress = (double) currentExerciseIndex / exercises.size();
        progressBar.setValue(progress);
        progressLabel.setText(currentExerciseIndex + " / " + exercises.size());
    }

    private void showNoExercisesMessage() {
        removeAll();

        VerticalLayout card = new VerticalLayout();
        card.setAlignItems(FlexComponent.Alignment.CENTER);
        card.setPadding(true);
        card.getStyle()
                .set("background-color", "white")
                .set("border-radius", "20px");

        H2 message = new H2("No exercises available for this lesson");
        Button backBtn = new Button("Back to Dashboard", e -> UI.getCurrent().navigate(""));

        card.add(message, backBtn);
        add(card);
    }
}
