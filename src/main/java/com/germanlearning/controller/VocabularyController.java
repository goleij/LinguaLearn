package com.germanlearning.controller;

import com.germanlearning.dto.ReviewWordRequest;
import com.germanlearning.dto.SaveWordRequest;
import com.germanlearning.dto.SavedWordDto;
import com.germanlearning.dto.WordBankDto;
import com.germanlearning.model.SavedWord;
import com.germanlearning.model.User;
import com.germanlearning.model.WordSource;
import com.germanlearning.service.CurrentUserService;
import com.germanlearning.service.VocabularyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * The word bank: the words a learner is collecting and the review queue built
 * from them.
 */
@RestController
@RequestMapping("/api/vocabulary")
public class VocabularyController {

    private final VocabularyService vocabularyService;
    private final CurrentUserService currentUserService;

    public VocabularyController(VocabularyService vocabularyService,
            CurrentUserService currentUserService) {
        this.vocabularyService = vocabularyService;
        this.currentUserService = currentUserService;
    }

    /** Everything in the bank, newest first, with the totals. */
    @GetMapping
    public WordBankDto getWordBank() {
        User user = currentUserService.requireCurrentUser();
        return WordBankDto.of(
                vocabularyService.stats(user.getId()),
                toDtos(vocabularyService.list(user.getId())));
    }

    /** Only the cards whose review date has come. */
    @GetMapping("/due")
    public List<SavedWordDto> getDue() {
        User user = currentUserService.requireCurrentUser();
        return toDtos(vocabularyService.due(user.getId()));
    }

    @PostMapping
    public ResponseEntity<SavedWordDto> addWord(@RequestBody SaveWordRequest request) {
        User user = currentUserService.requireCurrentUser();

        SavedWord saved = vocabularyService.save(
                user.getId(),
                request.german(),
                request.english(),
                parseSource(request.source()),
                request.topic(),
                request.lessonName());

        return ResponseEntity.status(HttpStatus.CREATED).body(SavedWordDto.from(saved));
    }

    /** One flashcard answer, which moves the word along its schedule. */
    @PostMapping("/{wordId}/review")
    public SavedWordDto review(@PathVariable Long wordId, @RequestBody ReviewWordRequest request) {
        User user = currentUserService.requireCurrentUser();
        boolean remembered = Boolean.TRUE.equals(request.remembered());

        return SavedWordDto.from(vocabularyService.review(user.getId(), wordId, remembered));
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<Void> remove(@PathVariable Long wordId) {
        User user = currentUserService.requireCurrentUser();
        vocabularyService.remove(user.getId(), wordId);
        return ResponseEntity.noContent().build();
    }

    private List<SavedWordDto> toDtos(List<SavedWord> words) {
        return words.stream().map(SavedWordDto::from).toList();
    }

    /** An unknown or missing source is simply a manual addition. */
    private WordSource parseSource(String source) {
        if (source == null || source.isBlank()) {
            return WordSource.MANUAL;
        }
        try {
            return WordSource.valueOf(source.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return WordSource.MANUAL;
        }
    }
}
