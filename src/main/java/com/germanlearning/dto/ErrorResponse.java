package com.germanlearning.dto;

/** Uniform error body so the SPA can always read `message`. */
public record ErrorResponse(String message) {
}
