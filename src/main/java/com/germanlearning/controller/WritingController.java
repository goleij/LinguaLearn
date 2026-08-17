package com.germanlearning.controller;

import com.germanlearning.dto.GrammarFeedbackDto;
import com.germanlearning.service.external.GrammarCheckService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * The Writing Coach.
 *
 * Always user triggered: the learner writes, presses the button, and only then
 * does the backend call LanguageTool.
 */
@RestController
@RequestMapping("/api/writing")
public class WritingController {

    private final GrammarCheckService grammarCheckService;

    public WritingController(GrammarCheckService grammarCheckService) {
        this.grammarCheckService = grammarCheckService;
    }

    @PostMapping("/check")
    public GrammarFeedbackDto check(@RequestBody GrammarCheckRequest request) {
        return grammarCheckService.check(request.text());
    }

    public record GrammarCheckRequest(String text) {
    }
}
