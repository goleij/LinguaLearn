package com.germanlearning.controller;

import com.germanlearning.dto.ExampleSentenceDto;
import com.germanlearning.service.external.TatoebaService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * Optional "Real examples" for a lesson word.
 *
 * Enrichment only: an empty list is a perfectly normal answer and the lesson
 * carries on without it.
 */
@RestController
@RequestMapping("/api/examples")
public class ExampleController {

    private final TatoebaService tatoebaService;

    public ExampleController(TatoebaService tatoebaService) {
        this.tatoebaService = tatoebaService;
    }

    @GetMapping
    public List<ExampleSentenceDto> examples(@RequestParam("query") String query) {
        return tatoebaService.findExamples(query);
    }
}
