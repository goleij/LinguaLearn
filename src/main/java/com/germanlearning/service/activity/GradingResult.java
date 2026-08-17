package com.germanlearning.service.activity;

/**
 * The verdict on one answer.
 *
 * @param correct        whether the answer is accepted
 * @param expectedAnswer what to show the learner when it is not
 * @param feedback       a specific, learner-friendly reason: what was wrong,
 *                       or what was already right. Never a bare "wrong".
 */
public record GradingResult(boolean correct, String expectedAnswer, String feedback) {

    public static GradingResult correct(String expectedAnswer, String feedback) {
        return new GradingResult(true, expectedAnswer, feedback);
    }

    public static GradingResult wrong(String expectedAnswer, String feedback) {
        return new GradingResult(false, expectedAnswer, feedback);
    }
}
