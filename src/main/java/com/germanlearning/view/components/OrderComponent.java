package com.germanlearning.view.components;

import com.germanlearning.model.OrderExercise;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import java.util.ArrayList;
import java.util.List;

public class OrderComponent extends ExerciseComponent {

    private final OrderExercise exercise;
    private final List<String> availableWords = new ArrayList<>();
    private final List<String> selectedWords = new ArrayList<>();
    private HorizontalLayout wordBank;
    private HorizontalLayout answerArea;
    private Button checkButton;

    public OrderComponent(OrderExercise exercise) {
        this.exercise = exercise;
        this.availableWords.addAll(exercise.getShuffledWordsList());
        buildUI();
    }

    private void buildUI() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(FlexComponent.Alignment.CENTER);

        H3 questionLabel = new H3("Arrange the words in correct order");
        questionLabel.getStyle().set("color", "#3C3C3C");

        Paragraph questionText = new Paragraph(exercise.getQuestion());
        questionText.getStyle()
                .set("font-size", "18px")
                .set("color", "#777");

        answerArea = new HorizontalLayout();
        answerArea.setWidthFull();
        answerArea.setMinHeight("60px");
        answerArea.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        answerArea.setAlignItems(FlexComponent.Alignment.CENTER);
        answerArea.getStyle()
                .set("background-color", "#F0F0F0")
                .set("border-radius", "12px")
                .set("padding", "15px")
                .set("flex-wrap", "wrap")
                .set("gap", "10px");

        Paragraph placeholder = new Paragraph("Tap words below to build the sentence");
        placeholder.getStyle().set("color", "#AFAFAF");
        placeholder.setId("answer-placeholder");
        answerArea.add(placeholder);

        wordBank = new HorizontalLayout();
        wordBank.setWidthFull();
        wordBank.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        wordBank.getStyle()
                .set("flex-wrap", "wrap")
                .set("gap", "10px")
                .set("margin-top", "20px");

        refreshWordBank();

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
            String answer = String.join(" ", selectedWords);
            submitAnswer(answer);
        });

        Button clearButton = new Button("Clear");
        clearButton.getStyle()
                .set("background-color", "#E5E5E5")
                .set("color", "#3C3C3C")
                .set("border-radius", "12px")
                .set("padding", "15px 30px")
                .set("margin-top", "20px");

        clearButton.addClickListener(e -> reset());

        HorizontalLayout buttonRow = new HorizontalLayout(clearButton, checkButton);
        buttonRow.setSpacing(true);

        add(questionLabel, questionText, answerArea, wordBank, buttonRow);
    }

    private void refreshWordBank() {
        wordBank.removeAll();
        
        for (String word : availableWords) {
            Div wordChip = createWordChip(word, true);
            wordBank.add(wordChip);
        }
    }

    private void refreshAnswerArea() {
        answerArea.removeAll();
        
        if (selectedWords.isEmpty()) {
            Paragraph placeholder = new Paragraph("Tap words below to build the sentence");
            placeholder.getStyle().set("color", "#AFAFAF");
            answerArea.add(placeholder);
        } else {
            for (String word : selectedWords) {
                Div wordChip = createWordChip(word, false);
                answerArea.add(wordChip);
            }
        }
        
        checkButton.setEnabled(!selectedWords.isEmpty() && availableWords.isEmpty());
    }

    private Div createWordChip(String word, boolean inBank) {
        Div chip = new Div();
        chip.getStyle()
                .set("padding", "10px 20px")
                .set("border-radius", "20px")
                .set("cursor", "pointer")
                .set("font-weight", "500")
                .set("transition", "all 0.2s");

        if (inBank) {
            chip.getStyle()
                    .set("background-color", "#1CB0F6")
                    .set("color", "white");
        } else {
            chip.getStyle()
                    .set("background-color", "#58CC02")
                    .set("color", "white");
        }

        Span label = new Span(word);
        chip.add(label);

        chip.addClickListener(e -> {
            if (inBank) {
                availableWords.remove(word);
                selectedWords.add(word);
            } else {
                selectedWords.remove(word);
                availableWords.add(word);
            }
            refreshWordBank();
            refreshAnswerArea();
        });

        return chip;
    }

    @Override
    public void reset() {
        selectedWords.clear();
        availableWords.clear();
        availableWords.addAll(exercise.getShuffledWordsList());
        refreshWordBank();
        refreshAnswerArea();
        checkButton.setEnabled(false);
    }
}
