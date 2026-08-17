package com.germanlearning.controller;

import com.germanlearning.dto.WordInfoDto;
import com.germanlearning.service.external.DictionaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** The Word Explorer endpoint behind which German Wiktionary sits. */
@RestController
@RequestMapping("/api/dictionary")
public class DictionaryController {

    private final DictionaryService dictionaryService;

    public DictionaryController(DictionaryService dictionaryService) {
        this.dictionaryService = dictionaryService;
    }

    @GetMapping("/{word}")
    public WordInfoDto lookup(@PathVariable String word) {
        return dictionaryService.lookup(word);
    }
}
