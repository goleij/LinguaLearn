package com.germanlearning.dto;

import com.germanlearning.service.VocabularyService.Stats;
import java.util.List;

/** The word bank page: the totals and the words themselves. */
public record WordBankDto(long total, long due, long learned, List<SavedWordDto> words) {

    public static WordBankDto of(Stats stats, List<SavedWordDto> words) {
        return new WordBankDto(stats.total(), stats.due(), stats.learned(), words);
    }
}
