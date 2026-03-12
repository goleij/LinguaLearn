package com.germanlearning.view.components;

import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import java.util.function.Consumer;

public abstract class ExerciseComponent extends VerticalLayout {

    protected Consumer<String> answerHandler;

    public void setAnswerHandler(Consumer<String> handler) {
        this.answerHandler = handler;
    }

    protected void submitAnswer(String answer) {
        if (answerHandler != null) {
            answerHandler.accept(answer);
        }
    }

    public abstract void reset();
}
