package com.germanlearning.view.components;

import com.germanlearning.model.MultipleChoiceExercise;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;

public class MultipleChoiceComponent extends ExerciseComponent {

    private final MultipleChoiceExercise exercise;
    private RadioButtonGroup<String> optionsGroup;
    private Button checkButton;

    public MultipleChoiceComponent(MultipleChoiceExercise exercise) {
        this.exercise = exercise;
        buildUI();
    }

    private void buildUI() {
        setWidthFull();
        setPadding(true);
        setSpacing(true);
        setAlignItems(FlexComponent.Alignment.CENTER);

        H3 questionLabel = new H3("Choose the correct answer");
        questionLabel.getStyle().set("color", "#3C3C3C");

        Paragraph questionText = new Paragraph(exercise.getQuestion());
        questionText.getStyle()
                .set("font-size", "24px")
                .set("font-weight", "bold")
                .set("color", "#1CB0F6")
                .set("text-align", "center");

        optionsGroup = new RadioButtonGroup<>();
        optionsGroup.setItems(exercise.getOptionsArray());
        optionsGroup.addThemeVariants(RadioGroupVariant.LUMO_VERTICAL);
        optionsGroup.getStyle()
                .set("width", "100%")
                .set("max-width", "400px");

        optionsGroup.addValueChangeListener(e -> {
            checkButton.setEnabled(e.getValue() != null);
        });

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
            String selectedOption = optionsGroup.getValue();
            if (selectedOption != null) {
                String[] options = exercise.getOptionsArray();
                int selectedIndex = -1;
                for (int i = 0; i < options.length; i++) {
                    if (options[i].equals(selectedOption)) {
                        selectedIndex = i;
                        break;
                    }
                }
                submitAnswer(String.valueOf(selectedIndex));
            }
        });

        add(questionLabel, questionText, optionsGroup, checkButton);
    }

    @Override
    public void reset() {
        optionsGroup.clear();
        checkButton.setEnabled(false);
    }
}
