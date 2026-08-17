package com.germanlearning.service.external;

import com.germanlearning.dto.WordInfoDto;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wiktionary is hand written, so the parser is judged on how it copes with
 * real shapes: a full entry, an entry for another language, and junk.
 */
class WiktionaryParserTest {

    private static final String HAUS_WIKITEXT = """
            == Haus ({{Sprache|Deutsch}}) ==
            === {{Wortart|Substantiv|Deutsch}}, {{n}} ===

            {{Deutsch Substantiv Übersicht
            |Genus=n
            |Nominativ Singular=Haus
            }}

            {{Bedeutungen}}
            :[1] ''[[Gebäude]], das Menschen als [[Wohnung]] dient''
            :[2] kurz für: [[Zuhause]]

            {{Herkunft}}
            :seit dem 8. Jahrhundert bezeugt

            {{Synonyme}}
            :[1] [[Gebäude]], [[Wohnhaus]]

            {{Beispiele}}
            :[1] Das '''Haus''' ist sehr groß.
            :[2] Ich gehe nach '''Haus'''.

            == Haus ({{Sprache|Englisch}}) ==
            === {{Wortart|Substantiv|Englisch}} ===
            {{Bedeutungen}}
            :[1] something else entirely
            """;

    @Test
    void readsTheGermanEntry() {
        WordInfoDto info = WiktionaryParser.parse("Haus", HAUS_WIKITEXT, "https://example.org");

        assertTrue(info.found());
        assertEquals("Substantiv", info.wordType());
        assertEquals("das", info.article());
        assertEquals("Gebäude, das Menschen als Wohnung dient", info.meanings().get(0));
        assertEquals("Das Haus ist sehr groß.", info.examples().get(0));
        assertTrue(info.synonyms().contains("Gebäude, Wohnhaus"));
        assertEquals("seit dem 8. Jahrhundert bezeugt", info.origin());
    }

    @Test
    void stopsAtTheNextLanguage() {
        WordInfoDto info = WiktionaryParser.parse("Haus", HAUS_WIKITEXT, "https://example.org");

        assertFalse(info.meanings().stream().anyMatch(meaning -> meaning.contains("something else")));
    }

    @Test
    void handlesAPageWithoutAGermanSection() {
        String english = """
                == dog ({{Sprache|Englisch}}) ==
                {{Bedeutungen}}
                :[1] a friendly animal
                """;

        WordInfoDto info = WiktionaryParser.parse("dog", english, "https://example.org");

        assertFalse(info.found());
        assertTrue(info.meanings().isEmpty());
    }

    @Test
    void handlesMissingAndEmptyInput() {
        assertFalse(WiktionaryParser.parse("nichts", null, "u").found());
        assertFalse(WiktionaryParser.parse("nichts", "   ", "u").found());
    }

    @Test
    void stripsWikiMarkup() {
        assertEquals("Gebäude, das dient",
                WiktionaryParser.cleanMarkup("''[[Gebäude]], das {{K|ugs.}} dient''<ref>x</ref>"));
    }
}
