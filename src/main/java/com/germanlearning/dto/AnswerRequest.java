package com.germanlearning.dto;

/**
 * Answer encoding: the selected index for multiple choice, the typed text for
 * fill-in-the-blank, "left:right;left:right" for matching and space separated
 * words for ordering.
 */
public record AnswerRequest(String answer) {
}
