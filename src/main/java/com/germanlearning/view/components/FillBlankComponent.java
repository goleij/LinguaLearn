package com.germanlearning.view.components;

import com.germanlearning.model.FillBlankExercise;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;

public class FillBlankComponent extends ExerciseComponent {

    private final FillBlankExercise exercise;
    private TextField answerField;
    private Button checkButton;

    public FillBlankComponent(FillBlankExercise exercise) {
        this.exercise = exercise;
        buildUI();
    }

    private void buildUI() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(FlexComponent.Alignment.CENTER);

        H3 questionLabel = new H3("Fill in the blank");
        questionLabel.getStyle().set("color", "#3C3C3C");

        Paragraph questionText = new Paragraph(exercise.getQuestion());
        questionText.getStyle()
                .set("font-size", "18px")
                .set("color", "#777");

        String sentence = exercise.getSentenceWithBlank();
        HorizontalLayout sentenceLayout = new HorizontalLayout();
        sentenceLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        sentenceLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        sentenceLayout.setWidthFull();
        sentenceLayout.getStyle().set("flex-wrap", "wrap");

        String[] parts = sentence.split("___");
        
        if (parts.length >= 1) {
            Span beforeBlank = new Span(parts[0]);
            beforeBlank.getStyle()
                    .set("font-size", "24px")
                    .set("color", "#3C3C3C");
            sentenceLayout.add(beforeBlank);
        }

        answerField = new TextField();
        answerField.setPlaceholder("...");
        answerField.setWidth("150px");
        answerField.getStyle()
                .set("font-size", "24px")
                .set("text-align", "center")
                .set("--vaadin-input-field-border-radius", "8px")
                .set("--vaadin-input-field-background", "#E5E5E5");
        
        answerField.addValueChangeListener(e -> {
            checkButton.setEnabled(!e.getValue().trim().isEmpty());
        });

        sentenceLayout.add(answerField);

        if (parts.length >= 2) {
            Span afterBlank = new Span(parts[1]);
            afterBlank.getStyle()
                    .set("font-size", "24px")
                    .set("color", "#3C3C3C");
            sentenceLayout.add(afterBlank);
        }

        checkButton = new Button("Check Answer");
        checkButton.setEnabled(false);
        checkButton.getStyle()
                .set("background-color", "#58CC02")
                .set("color", "white")
                .set("border-radius", "12px")
                .set("padding", "15px 40px")
                .set("font-weight", "bold")
                .set("font-size", "16px")
                .set("margin-top", "20px");

        checkButton.addClickListener(e -> {
            String answer = answerField.getValue().trim();
            if (!answer.isEmpty()) {
                submitAnswer(answer);
            }
        });

        answerField.addKeyPressListener(com.vaadin.flow.component.Key.ENTER, e -> {
            if (!answerField.getValue().trim().isEmpty()) {
                submitAnswer(answerField.getValue().trim());
            }
        });

        add(questionLabel, questionText, sentenceLayout, checkButton);
    }

    @Override
    public void reset() {
        answerField.clear();
        checkButton.setEnabled(false);
    }
}
