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
 * A2 — the same situation as the A1 restaurant lesson, one level up.
 *
 * Where A1 orders a coffee, A2 complains about the food: dative verbs, the
 * perfect tense and longer sentences.
 */
@Component
public class A2RestaurantContent extends CourseContent {

    private static final CefrLevel LEVEL = CefrLevel.A2;
    private static final String TOPIC = "restaurant";

    @Override
    public String courseName() {
        return "German A2";
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public void seed() {
        Course course = course(courseName(), LEVEL,
                "Say what happened, what you think and what you want changed");
        Unit unit = unit(course, "Im Alltag", 0, "Everyday situations that need more than one sentence");
        Lesson lesson = lesson(unit, "Im Restaurant reklamieren", 0,
                "Complain politely, talk about what you already ordered", LEVEL);

        add(lesson,
                // ---------------------------------------------------- LEARN
                make.vocabulary("Wenn etwas nicht stimmt",
                        "Words for when the food or the service is not right.",
                        List.of(
                                make.word("reklamieren", "to complain (about a product)"),
                                make.word("die Beschwerde", "the complaint"),
                                make.word("versalzen", "too salty"),
                                make.word("die Bedienung", "the service / the waiting staff"),
                                make.word("das Trinkgeld", "the tip"),
                                make.word("umtauschen", "to exchange"),
                                make.word("sich beschweren", "to complain")),
                        LEVEL, TOPIC),

                make.grammarTip("Verbs that take the dative",
                        "Some very common verbs do not say what you do to something, but how "
                                + "something is for you. They take the dative: mir, dir, ihm, ihr, uns.",
                        List.of("Das Essen schmeckt mir nicht. — I don't like the taste.",
                                "Der Service gefällt mir nicht. — I don't like the service.",
                                "Das ist mir zu salzig. — That is too salty for me.",
                                "Compare: Ich esse die Suppe (accusative) vs. Die Suppe schmeckt mir (dative)."),
                        LEVEL, Skill.GRAMMAR, TOPIC),

                make.grammarTip("Talking about what already happened",
                        "German usually speaks about the past with the perfect tense: haben or "
                                + "sein plus the participle at the end of the sentence.",
                        List.of("Ich habe die Suppe bestellt. — I ordered the soup.",
                                "Wir haben lange gewartet. — We waited a long time.",
                                "The participle goes last: Ich habe das Essen schon bezahlt."),
                        LEVEL, Skill.VERB_CONJUGATION, TOPIC),

                // -------------------------------------------------- CONTEXT
                make.dialogueScene("Die Suppe ist kalt",
                        "Markus has been waiting and the soup arrives cold.",
                        List.of(
                                make.line("Markus", "Entschuldigung, die Suppe ist leider kalt."),
                                make.line("Kellnerin", "Das tut mir leid. Möchten Sie eine neue?"),
                                make.line("Markus", "Ja, gern. Ich habe schon zwanzig Minuten gewartet."),
                                make.line("Kellnerin", "Ich bringe Ihnen sofort eine warme Suppe."),
                                make.line("Markus", "Danke. Und könnten Sie bitte die Rechnung mitbringen?")),
                        LEVEL, TOPIC),

                make.realExamples("„Rechnung“ in real sentences",
                        "How the word turns up outside the lesson.",
                        "Rechnung",
                        List.of(
                                make.example("Können wir bitte die Rechnung haben?",
                                        "Could we have the bill, please?"),
                                make.example("Die Rechnung stimmt nicht.", "The bill is not correct.")),
                        LEVEL, TOPIC),

                // ------------------------------------------- GUIDED PRACTICE
                make.fillBlank(ActivityPhase.GUIDED_PRACTICE,
                        "Say that you do not like the taste",
                        "Das Essen schmeckt ___ nicht.", "mir",
                        "„Schmecken“ takes the dative: the taste is a matter of opinion, so it is "
                                + "„mir“, not „mich“.",
                        "Which dative pronoun means 'to me'?",
                        Skill.GRAMMAR, Difficulty.MEDIUM, LEVEL, TOPIC),

                make.choice(ActivityType.CONTEXT_CHOICE, ActivityPhase.GUIDED_PRACTICE,
                        "Choose what fits the situation",
                        "How do you complain politely?",
                        "The steak you ordered is far too salty.",
                        List.of("Das Steak ist schlecht!", "Entschuldigung, das Steak ist mir zu salzig.",
                                "Ich will ein neues Steak!", "Das Steak schmeckt mich nicht."),
                        1,
                        "„Entschuldigung“ plus a dative sentence stays friendly and is grammatically right.",
                        "Start with an apology word and use the dative.",
                        Skill.GRAMMAR, Difficulty.MEDIUM, LEVEL, TOPIC),

                // ------------------------------------------------- PRACTICE
                make.sentenceBuilder(ActivityPhase.PRACTICE,
                        "Say: I already ordered the soup.",
                        List.of("bestellt", "Ich", "die", "habe", "Suppe", "schon"),
                        "Ich habe die Suppe schon bestellt",
                        "In the perfect tense the participle „bestellt“ goes to the very end.",
                        null, Difficulty.MEDIUM, LEVEL, TOPIC),

                make.translation(ActivityPhase.PRACTICE, "Translate into German",
                        "We waited thirty minutes.", "Wir haben dreißig Minuten gewartet",
                        "Perfect tense with „haben“ and the participle at the end.",
                        null, Difficulty.HARD, LEVEL, TOPIC),

                make.fillBlank(ActivityPhase.PRACTICE,
                        "Ask for something to be exchanged",
                        "Könnten Sie das bitte ___?", "umtauschen",
                        "„Umtauschen“ is the standard word for exchanging something you were given.",
                        null, Skill.VOCABULARY, Difficulty.MEDIUM, LEVEL, TOPIC),

                // ---------------------------------------------------- APPLY
                make.shortWriting("Beschweren Sie sich höflich.",
                        "The main course arrived cold and late. Write a short, polite complaint "
                                + "to the waiter. Use „Entschuldigung“ and say what you already did.",
                        12, List.of("entschuldigung"),
                        "For example: „Entschuldigung, das Hauptgericht ist kalt. Ich habe schon "
                                + "lange gewartet. Könnten Sie es bitte umtauschen?“",
                        LEVEL, TOPIC),

                // ----------------------------------------------- CHECKPOINT
                make.checkpointCard("Checkpoint",
                        "Three tasks on the dative and the perfect tense.", LEVEL),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.CHECKPOINT,
                        "Choose the correct answer",
                        "Which sentence is correct?", null,
                        List.of("Ich habe die Rechnung bezahlt.", "Ich habe bezahlt die Rechnung.",
                                "Ich bin die Rechnung bezahlt.", "Ich habe die Rechnung bezahlen."),
                        0,
                        "Perfect tense: haben + participle, and the participle goes last.",
                        null, Skill.VERB_CONJUGATION, Difficulty.MEDIUM, LEVEL, TOPIC),

                make.fillBlank(ActivityPhase.CHECKPOINT,
                        "Complete the complaint",
                        "Der Wein ist ___ zu warm.", "mir",
                        "„Mir“ marks whose opinion it is — the dative again.",
                        null, Skill.GRAMMAR, Difficulty.HARD, LEVEL, TOPIC),

                make.matchPairs(ActivityPhase.CHECKPOINT,
                        "Match the German expressions with their meanings",
                        make.pairs(
                                "sich beschweren", "to complain",
                                "das Trinkgeld", "the tip",
                                "die Bedienung", "the service",
                                "umtauschen", "to exchange"),
                        "The vocabulary you need to sort out a problem in a restaurant.",
                        Skill.VOCABULARY, Difficulty.MEDIUM, LEVEL, TOPIC));
    }
}
