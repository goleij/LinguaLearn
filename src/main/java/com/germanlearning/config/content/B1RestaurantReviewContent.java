package com.germanlearning.config.content;

import com.germanlearning.model.ActivityPhase;
import com.germanlearning.model.ActivityType;
import com.germanlearning.model.CefrLevel;
import com.germanlearning.model.Course;
import com.germanlearning.model.Difficulty;
import com.germanlearning.model.Lesson;
import com.germanlearning.model.Skill;
import com.germanlearning.model.Unit;
import org.springframework.stereotype.Component;
import java.util.List;

/**
 * B1 — expressing an opinion and justifying it.
 *
 * The step up from A2: Konjunktiv II for politeness and subordinate clauses
 * where the verb moves to the end.
 */
@Component
public class B1RestaurantReviewContent extends CourseContent {

    private static final CefrLevel LEVEL = CefrLevel.B1;
    private static final String TOPIC = "opinion";

    @Override
    public String courseName() {
        return "German B1";
    }

    @Override
    public int order() {
        return 40;
    }

    @Override
    public void seed() {
        Course course = course(courseName(), LEVEL,
                "Give opinions, tell a longer story and connect your ideas");
        Unit unit = unit(course, "Meinung und Erfahrung", 0, "Say what you think and why");
        Lesson lesson = lesson(unit, "Eine Restaurantkritik schreiben", 0,
                "Review a restaurant: judge it, justify it, stay polite", LEVEL);

        add(lesson,
                // ---------------------------------------------------- LEARN
                make.vocabulary("Wortschatz für Kritiken",
                        "Words that let you judge rather than only describe.",
                        List.of(
                                make.word("das Ambiente", "the atmosphere"),
                                make.word("aufmerksam", "attentive"),
                                make.word("enttäuschend", "disappointing"),
                                make.word("die Portion", "the portion"),
                                make.word("empfehlen", "to recommend"),
                                make.word("der Preis-Leistungs-Verhältnis", "value for money"),
                                make.word("zuvorkommend", "courteous")),
                        LEVEL, TOPIC),

                make.grammarTip("Konjunktiv II: polite and hypothetical",
                        "Konjunktiv II makes a request softer and lets you talk about what would "
                                + "be. The three you need constantly are hätte, wäre and würde.",
                        List.of("Ich hätte gern einen Tisch am Fenster. — I would like a table by the window.",
                                "Das wäre schön. — That would be nice.",
                                "Ich würde das Restaurant empfehlen. — I would recommend the restaurant.",
                                "Compare: Ich will (blunt) → Ich hätte gern (polite)."),
                        LEVEL, Skill.GRAMMAR, TOPIC),

                make.learnCard("Connecting your reasons",
                        "A review is an opinion plus a reason. In German the connector decides "
                                + "where the verb goes.",
                        null,
                        List.of("weil — verb goes to the END: … weil das Essen gut war.",
                                "obwohl — verb goes to the END: Obwohl der Service langsam war, …",
                                "deshalb — verb stays SECOND: Das Essen war gut, deshalb komme ich wieder.",
                                "trotzdem — verb stays SECOND: Der Service war langsam, trotzdem war es schön."),
                        LEVEL, TOPIC),

                // -------------------------------------------------- CONTEXT
                make.dialogueScene("Zwei Meinungen",
                        "Two friends disagree about the same restaurant.",
                        List.of(
                                make.line("Lena", "Ich würde das Restaurant empfehlen, weil das Essen "
                                        + "wirklich frisch war."),
                                make.line("Tom", "Obwohl der Service sehr langsam war?"),
                                make.line("Lena", "Das stimmt, aber die Bedienung war zuvorkommend."),
                                make.line("Tom", "Für den Preis hätte ich mehr erwartet."),
                                make.line("Lena", "Trotzdem gehe ich wieder hin.")),
                        LEVEL, TOPIC),

                make.realExamples("„empfehlen“ in real sentences",
                        "See the verb used by other writers.",
                        "empfehlen",
                        List.of(
                                make.example("Ich kann dieses Restaurant sehr empfehlen.",
                                        "I can really recommend this restaurant."),
                                make.example("Was können Sie mir empfehlen?",
                                        "What can you recommend to me?")),
                        LEVEL, TOPIC),

                // ------------------------------------------- GUIDED PRACTICE
                make.fillBlank(ActivityPhase.GUIDED_PRACTICE,
                        "Reserve a table politely",
                        "Ich ___ gern einen Tisch reserviert.", "hätte",
                        "„Ich hätte gern“ is the standard polite request — Konjunktiv II of haben.",
                        "It is the Konjunktiv II form of 'haben'.",
                        Skill.GRAMMAR, Difficulty.MEDIUM, LEVEL, TOPIC),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.GUIDED_PRACTICE,
                        "Choose the correct sentence",
                        "Which sentence puts the verb in the right place?", null,
                        List.of("Ich komme wieder, weil das Essen war gut.",
                                "Ich komme wieder, weil das Essen gut war.",
                                "Ich komme wieder, weil war das Essen gut.",
                                "Weil ich komme wieder, das Essen gut war."),
                        1,
                        "After „weil“ the conjugated verb moves to the very end of the clause.",
                        "Remember: weil sends the verb to the end.",
                        Skill.WORD_ORDER, Difficulty.MEDIUM, LEVEL, TOPIC),

                // ------------------------------------------------- PRACTICE
                make.sentenceBuilder(ActivityPhase.PRACTICE,
                        "Build the clause: although the food was good",
                        List.of("war", "Obwohl", "das", "Essen", "gut"),
                        "Obwohl das Essen gut war",
                        "„Obwohl“ is a subordinating conjunction, so „war“ lands at the end.",
                        null, Difficulty.HARD, LEVEL, TOPIC),

                make.translation(ActivityPhase.PRACTICE, "Translate into German",
                        "The service was disappointing.", "Der Service war enttäuschend",
                        "„Enttäuschend“ is the adjective you need for a critical review.",
                        null, Difficulty.MEDIUM, LEVEL, TOPIC),

                make.choice(ActivityType.CONTEXT_CHOICE, ActivityPhase.PRACTICE,
                        "Choose what fits the situation",
                        "Which sentence belongs in a written review?",
                        "You are writing a public review and want to stay fair but critical.",
                        List.of("Das Essen war voll schlecht, echt.",
                                "Das Preis-Leistungs-Verhältnis war leider enttäuschend.",
                                "Ich will mein Geld zurück!!!",
                                "Der Koch kann gar nichts."),
                        1,
                        "A review stays factual: name the aspect, then judge it.",
                        null, Skill.READING, Difficulty.HARD, LEVEL, TOPIC),

                // ---------------------------------------------------- APPLY
                make.shortWriting("Schreiben Sie eine kurze Restaurantkritik.",
                        "Two to three sentences: what was good, what was not, and whether you "
                                + "would go again. Use „weil“ to justify at least one judgement.",
                        20, List.of("weil"),
                        "For example: „Das Ambiente war schön und die Bedienung zuvorkommend. "
                                + "Der Service war aber langsam, weil das Restaurant sehr voll war. "
                                + "Trotzdem würde ich wiederkommen.“",
                        LEVEL, TOPIC),

                // ----------------------------------------------- CHECKPOINT
                make.checkpointCard("Checkpoint",
                        "Three tasks on Konjunktiv II and clause order.", LEVEL),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.CHECKPOINT,
                        "Choose the polite version",
                        "You want a table by the window. What do you say?", null,
                        List.of("Ich will einen Tisch am Fenster.",
                                "Ich hätte gern einen Tisch am Fenster.",
                                "Gib mir einen Tisch am Fenster.",
                                "Ich habe gern einen Tisch am Fenster."),
                        1,
                        "„Ich hätte gern“ — Konjunktiv II is what makes it polite.",
                        null, Skill.GRAMMAR, Difficulty.MEDIUM, LEVEL, TOPIC),

                make.fillBlank(ActivityPhase.CHECKPOINT,
                        "Finish the reason",
                        "Ich empfehle das Restaurant, weil das Essen frisch ___.", "ist|war",
                        "After „weil“ the verb is last — „ist“ or „war“, depending on the tense.",
                        null, Skill.WORD_ORDER, Difficulty.HARD, LEVEL, TOPIC),

                make.matchPairs(ActivityPhase.CHECKPOINT,
                        "Match each connector with what it does to the verb",
                        make.pairs(
                                "weil", "verb goes to the end",
                                "deshalb", "verb stays in second position",
                                "obwohl", "concession, verb at the end",
                                "trotzdem", "concession, verb in second position"),
                        "Getting the verb position right is what separates B1 from A2 writing.",
                        Skill.WORD_ORDER, Difficulty.HARD, LEVEL, TOPIC));
    }
}
