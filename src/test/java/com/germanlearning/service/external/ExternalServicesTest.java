package com.germanlearning.service.external;

import com.germanlearning.dto.ExampleSentenceDto;
import com.germanlearning.dto.GrammarFeedbackDto;
import com.germanlearning.dto.WordInfoDto;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.hamcrest.Matchers.containsString;

/**
 * The three integrations, exercised against stubbed responses: no network is
 * touched, and the point of each test is what the learner ends up seeing.
 */
class ExternalServicesTest {

    private final ExternalApiProperties properties = new ExternalApiProperties();

    private record Fixture(RestClient client, MockRestServiceServer server) {
    }

    private Fixture fixture() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        return new Fixture(builder.build(), server);
    }

    // ------------------------------------------------------------- Tatoeba

    @Test
    void tatoebaKeepsOnlyUsableSentences() {
        String json = """
                {"results":[
                  {"text":"Ich möchte einen Kaffee bestellen.","lang":"deu",
                   "translations":[[{"lang":"eng","text":"I would like to order a coffee."}]]},
                  {"text":"Zu kurz.","lang":"deu",
                   "translations":[[{"lang":"eng","text":"Too short."}]]},
                  {"text":"Wir bestellen die Vorspeise später am Abend im Restaurant.","lang":"deu",
                   "translations":[[{"lang":"fra","text":"Nous commandons."}]]},
                  {"text":"Bitte bestellen Sie unter http://example.org sofort.","lang":"deu",
                   "translations":[[{"lang":"eng","text":"Order at the link."}]]},
                  {"text":"Heute gehen wir ins Kino und danach essen.","lang":"deu",
                   "translations":[[{"lang":"eng","text":"Today we go to the cinema."}]]}
                ]}
                """;

        Fixture fixture = fixture();
        fixture.server().expect(requestTo(containsString("query=bestellen")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        List<ExampleSentenceDto> examples =
                new TatoebaService(fixture.client(), properties).findExamples("bestellen");

        // Only the first survives: long enough, has an English translation,
        // contains the word, and carries no link
        assertEquals(1, examples.size());
        assertEquals("Ich möchte einen Kaffee bestellen.", examples.get(0).german());
        assertEquals("I would like to order a coffee.", examples.get(0).english());
        assertEquals("Tatoeba", examples.get(0).source());
    }

    @Test
    void tatoebaFailureIsNotFatal() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(containsString("query=haus")))
                .andRespond(withServerError());

        assertTrue(new TatoebaService(fixture.client(), properties).findExamples("haus").isEmpty());
    }

    // ---------------------------------------------------------- Wiktionary

    @Test
    void dictionaryReadsAnEntry() {
        String json = """
                {"parse":{"title":"Haus","wikitext":"== Haus ({{Sprache|Deutsch}}) ==\\n\
                === {{Wortart|Substantiv|Deutsch}} ===\\n{{Genus|n}}\\n\
                {{Bedeutungen}}\\n:[1] Gebäude zum Wohnen\\n"}}
                """;

        Fixture fixture = fixture();
        fixture.server().expect(requestTo(containsString("page=Haus")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        WordInfoDto info = new DictionaryService(fixture.client(), properties).lookup("Haus");

        assertTrue(info.found());
        assertEquals("das", info.article());
        assertEquals("Gebäude zum Wohnen", info.meanings().get(0));
    }

    @Test
    void dictionaryReportsAMissingPageWithoutFailing() {
        String json = """
                {"error":{"code":"missingtitle","info":"The page you specified doesn't exist."}}
                """;

        Fixture fixture = fixture();
        fixture.server().expect(requestTo(containsString("page=Quatschwort")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        WordInfoDto info = new DictionaryService(fixture.client(), properties).lookup("Quatschwort");

        assertFalse(info.found());
        assertTrue(info.meanings().isEmpty());
        assertTrue(info.sourceUrl().contains("Quatschwort"));
    }

    @Test
    void dictionaryStripsPunctuationFromClickedWords() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(containsString("page=Rechnung")))
                .andRespond(withSuccess("{\"parse\":{\"wikitext\":\"\"}}", MediaType.APPLICATION_JSON));

        WordInfoDto info = new DictionaryService(fixture.client(), properties).lookup("„Rechnung,“");

        assertEquals("Rechnung", info.word());
    }

    // -------------------------------------------------------- LanguageTool

    @Test
    void grammarCheckTurnsMatchesIntoLearnerFeedback() {
        String json = """
                {"matches":[
                  {"message":"Möglicher Tippfehler gefunden.","shortMessage":"Rechtschreibfehler",
                   "offset":4,"length":6,
                   "replacements":[{"value":"möchte"},{"value":"mochte"},{"value":"machte"},{"value":"nochte"}],
                   "rule":{"id":"GERMAN_SPELLER_RULE","category":{"name":"Mögliche Tippfehler"}}}
                ]}
                """;

        Fixture fixture = fixture();
        fixture.server().expect(requestTo(containsString("/check")))
                .andRespond(withSuccess(json, MediaType.APPLICATION_JSON));

        GrammarFeedbackDto feedback =
                new GrammarCheckService(fixture.client(), properties).check("Ich mochte einen Kaffee.");

        assertTrue(feedback.available());
        assertEquals(1, feedback.issueCount());
        assertEquals("mochte", feedback.issues().get(0).excerpt());
        assertEquals("Mögliche Tippfehler", feedback.issues().get(0).category());
        // At most three suggestions reach the learner
        assertEquals(3, feedback.issues().get(0).suggestions().size());
        assertEquals("One thing to look at.", feedback.message());
    }

    @Test
    void grammarCheckSaysSoWhenTheServiceIsDown() {
        Fixture fixture = fixture();
        fixture.server().expect(requestTo(containsString("/check")))
                .andRespond(withServerError());

        GrammarFeedbackDto feedback =
                new GrammarCheckService(fixture.client(), properties).check("Ich bin müde.");

        assertFalse(feedback.available());
        assertTrue(feedback.message().contains("could not be reached"));
    }

    @Test
    void grammarCheckRejectsEmptyAndOverlongText() {
        Fixture fixture = fixture();
        GrammarCheckService service = new GrammarCheckService(fixture.client(), properties);

        assertFalse(service.check("   ").available());
        assertFalse(service.check("x".repeat(2000)).available());
    }
}
