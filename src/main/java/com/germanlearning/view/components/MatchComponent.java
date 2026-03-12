package com.germanlearning.view.components;

import com.germanlearning.model.MatchExercise;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.*;

public class MatchComponent extends ExerciseComponent {

    private final MatchExercise exercise;
    private final Map<String, String> userMatches = new LinkedHashMap<>();
    private String selectedLeft = null;
    private final Map<String, Div> leftButtons = new HashMap<>();
    private final Map<String, Div> rightButtons = new HashMap<>();
    private Button checkButton;

    public MatchComponent(MatchExercise exercise) {
        this.exercise = exercise;
        buildUI();
    }

    private void buildUI() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(FlexComponent.Alignment.CENTER);

        H3 questionLabel = new H3("Match the pairs");
        questionLabel.getStyle().set("color", "#3C3C3C");

        Paragraph questionText = new Paragraph(exercise.getQuestion());
        questionText.getStyle()
                .set("font-size", "18px")
                .set("color", "#777");

        Map<String, String> pairs = exercise.getPairs();
        List<String> leftItems = new ArrayList<>(pairs.keySet());
        List<String> rightItems = new ArrayList<>(pairs.values());
        Collections.shuffle(rightItems);

        HorizontalLayout columnsLayout = new HorizontalLayout();
        columnsLayout.setWidthFull();
        columnsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        columnsLayout.setSpacing(true);

        VerticalLayout leftColumn = new VerticalLayout();
        leftColumn.setAlignItems(FlexComponent.Alignment.CENTER);
        leftColumn.setSpacing(true);
        leftColumn.setWidth("200px");

        VerticalLayout rightColumn = new VerticalLayout();
        rightColumn.setAlignItems(FlexComponent.Alignment.CENTER);
        rightColumn.setSpacing(true);
        rightColumn.setWidth("200px");

        for (String item : leftItems) {
            Div btn = createMatchButton(item, true);
            leftButtons.put(item, btn);
            leftColumn.add(btn);
        }

        for (String item : rightItems) {
            Div btn = createMatchButton(item, false);
            rightButtons.put(item, btn);
            rightColumn.add(btn);
        }

        columnsLayout.add(leftColumn, rightColumn);

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
            StringBuilder answer = new StringBuilder();
            for (Map.Entry<String, String> entry : userMatches.entrySet()) {
                if (answer.length() > 0) {
                    answer.append(";");
                }
                answer.append(entry.getKey()).append(":").append(entry.getValue());
            }
            submitAnswer(answer.toString());
        });

        add(questionLabel, questionText, columnsLayout, checkButton);
    }

    private Div createMatchButton(String text, boolean isLeft) {
        Div btn = new Div();
        btn.setWidth("180px");
        btn.getStyle()
                .set("padding", "15px")
                .set("text-align", "center")
                .set("border-radius", "12px")
                .set("cursor", "pointer")
                .set("background-color", "#E5E5E5")
                .set("color", "#3C3C3C")
                .set("font-weight", "500")
                .set("transition", "all 0.2s");

        Span label = new Span(text);
        btn.add(label);

        btn.addClickListener(e -> handleClick(text, isLeft, btn));

        return btn;
    }

    private void handleClick(String text, boolean isLeft, Div btn) {
        if (isLeft) {
            if (selectedLeft != null && leftButtons.containsKey(selectedLeft)) {
                leftButtons.get(selectedLeft).getStyle()
                        .set("background-color", "#E5E5E5")
                        .set("border", "none");
            }
            
            selectedLeft = text;
            btn.getStyle()
                    .set("background-color", "#1CB0F6")
                    .set("color", "white");
        } else {
            if (selectedLeft != null) {
                userMatches.put(selectedLeft, text);
                
                leftButtons.get(selectedLeft).getStyle()
                        .set("background-color", "#58CC02")
                        .set("color", "white");
                btn.getStyle()
                        .set("background-color", "#58CC02")
                        .set("color", "white");
                
                selectedLeft = null;
                
                Map<String, String> pairs = exercise.getPairs();
                checkButton.setEnabled(userMatches.size() == pairs.size());
            }
        }
    }

    @Override
    public void reset() {
        userMatches.clear();
        selectedLeft = null;
        
        for (Div btn : leftButtons.values()) {
            btn.getStyle()
                    .set("background-color", "#E5E5E5")
                    .set("color", "#3C3C3C");
        }
        for (Div btn : rightButtons.values()) {
            btn.getStyle()
                    .set("background-color", "#E5E5E5")
                    .set("color", "#3C3C3C");
        }
        
        checkButton.setEnabled(false);
    }
}
