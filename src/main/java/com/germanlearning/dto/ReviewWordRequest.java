package com.germanlearning.dto;

/** One flashcard answer: did the learner remember it or not. */
public record ReviewWordRequest(Boolean remembered) {
}
