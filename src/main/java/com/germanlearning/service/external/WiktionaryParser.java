package com.germanlearning.service.external;

import com.germanlearning.dto.WordInfoDto;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Turns a German Wiktionary page into something a learner can read.
 *
 * Wiktionary is hand written wiki markup, not a database: sections are
 * optional, ordering varies and templates are used inconsistently. So this
 * parser is deliberately forgiving — it takes what it recognises, ignores what
 * it does not, and never throws. Anything it cannot find simply comes back
 * empty.
 *
 * Kept separate from the HTTP call so the parsing rules can be tested against
 * fixed samples.
 */
public final class WiktionaryParser {

    private static final Pattern GERMAN_SECTION =
            Pattern.compile("^==\\s*[^=]*\\(\\{\\{Sprache\\|Deutsch\\}\\}\\)\\s*==\\s*$", Pattern.MULTILINE);
    private static final Pattern NEXT_LANGUAGE_SECTION =
            Pattern.compile("^==\\s*[^=]*\\(\\{\\{Sprache\\|(?!Deutsch)", Pattern.MULTILINE);

    private static final Pattern WORD_TYPE = Pattern.compile("\\{\\{Wortart\\|([^|}]+)");
    private static final Pattern GENUS_TEMPLATE = Pattern.compile("\\{\\{Genus\\|?([mfn])?\\}\\}");
    private static final Pattern GENUS_FIELD = Pattern.compile("\\|\\s*Genus\\s*=\\s*([mfn])");
    /** Leading ":" plus the optional "[1]" numbering some sections use. */
    private static final Pattern LIST_MARKER =
            Pattern.compile("^:+\\s*(\\[[0-9a-z,\\s\\-–]*\\]\\s*)?");

    private WiktionaryParser() {
    }

    public static WordInfoDto parse(String word, String wikitext, String sourceUrl) {
        if (wikitext == null || wikitext.isBlank()) {
            return WordInfoDto.notFound(word, sourceUrl);
        }

        String german = germanSection(wikitext);
        if (german.isBlank()) {
            return WordInfoDto.notFound(word, sourceUrl);
        }

        String wordType = firstMatch(WORD_TYPE, german);
        String article = articleFor(german);
        List<String> meanings = listSection(german, "Bedeutungen");
        List<String> examples = listSection(german, "Beispiele");
        List<String> synonyms = listSection(german, "Synonyme");
        List<String> originLines = listSection(german, "Herkunft");

        boolean found = wordType != null || !meanings.isEmpty() || !examples.isEmpty();

        return new WordInfoDto(
                word,
                found,
                wordType,
                article,
                meanings,
                examples,
                synonyms,
                originLines.isEmpty() ? null : originLines.get(0),
                "German Wiktionary",
                sourceUrl);
    }

    /** Wiktionary pages hold several languages; only the German part is ours. */
    private static String germanSection(String wikitext) {
        Matcher start = GERMAN_SECTION.matcher(wikitext);
        if (!start.find()) {
            // Some pages only ever describe one language and skip the marker
            return wikitext.contains("{{Sprache|Deutsch}}") ? wikitext : "";
        }

        String rest = wikitext.substring(start.end());
        Matcher next = NEXT_LANGUAGE_SECTION.matcher(rest);
        return next.find() ? rest.substring(0, next.start()) : rest;
    }

    private static String articleFor(String german) {
        String genus = firstMatch(GENUS_TEMPLATE, german);
        if (genus == null) {
            genus = firstMatch(GENUS_FIELD, german);
        }
        if (genus == null) {
            return null;
        }
        return switch (genus) {
            case "m" -> "der";
            case "f" -> "die";
            case "n" -> "das";
            default -> null;
        };
    }

    /**
     * Reads a "{{Bedeutungen}}" style block: the lines that follow it until the
     * next template heading or the end of the section.
     */
    private static List<String> listSection(String german, String sectionName) {
        List<String> entries = new ArrayList<>();
        int start = german.indexOf("{{" + sectionName + "}}");
        if (start < 0) {
            return entries;
        }

        String[] lines = german.substring(start).split("\\R");
        Set<String> seen = new LinkedHashSet<>();

        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) {
                continue;
            }
            // The next section starts with its own template or a heading
            if (line.startsWith("{{") || line.startsWith("==")) {
                break;
            }
            if (!line.startsWith(":")) {
                continue;
            }

            String cleaned = cleanMarkup(LIST_MARKER.matcher(line).replaceFirst(""));
            if (!cleaned.isBlank() && seen.add(cleaned)) {
                entries.add(cleaned);
            }
            if (entries.size() >= 5) {
                break;
            }
        }

        return entries;
    }

    /** Strips the wiki syntax a learner should never see. */
    static String cleanMarkup(String text) {
        String cleaned = text;
        cleaned = cleaned.replaceAll("<ref[^>]*>.*?</ref>", "");
        cleaned = cleaned.replaceAll("<ref[^>]*/>", "");
        cleaned = cleaned.replaceAll("<[^>]+>", "");
        cleaned = cleaned.replaceAll("\\{\\{[^}]*\\}\\}", "");
        cleaned = cleaned.replaceAll("\\[\\[[^\\]|]*\\|([^\\]]*)\\]\\]", "$1");
        cleaned = cleaned.replaceAll("\\[\\[([^\\]]*)\\]\\]", "$1");
        cleaned = cleaned.replace("'''", "").replace("''", "");
        cleaned = cleaned.replace("&nbsp;", " ").replace("&amp;", "&");
        cleaned = cleaned.replaceAll("\\s+", " ");
        return cleaned.trim();
    }

    private static String firstMatch(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (matcher.find() && matcher.group(1) != null && !matcher.group(1).isBlank()) {
            return matcher.group(1).trim();
        }
        return null;
    }
}
