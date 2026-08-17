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
 * A1 — At the Restaurant.
 *
 * The reference lesson for the new flow: the learner meets the words, sees
 * them used, tries them with help, practises without help, writes something of
 * their own and only then is assessed.
 */
@Component
public class A1RestaurantContent extends CourseContent {

    private static final CefrLevel LEVEL = CefrLevel.A1;
    private static final String TOPIC = "restaurant";

    @Override
    public String courseName() {
        return "Everyday German A1";
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public void seed() {
        Course course = course(courseName(), LEVEL,
                "Handle everyday situations in German: eating out, shopping, getting around");
        Unit unit = unit(course, "Out and About", 0, "Situations you meet on your first trip");
        Lesson lesson = lesson(unit, "At the Restaurant", 0,
                "Order food and drinks, ask for the bill and pay", LEVEL);

        add(lesson,
                // ---------------------------------------------------- LEARN
                make.vocabulary("Words you need at the table",
                        "Nine words that cover almost every restaurant visit.",
                        List.of(
                                make.word("die Speisekarte", "the menu"),
                                make.word("der Kellner / die Kellnerin", "the waiter / waitress"),
                                make.word("bestellen", "to order"),
                                make.word("die Rechnung", "the bill"),
                                make.word("das Wasser", "the water"),
                                make.word("der Tisch", "the table"),
                                make.word("die Vorspeise", "the starter"),
                                make.word("das Hauptgericht", "the main course"),
                                make.word("bezahlen", "to pay")),
                        LEVEL, TOPIC),

                make.grammarTip("Ordering politely: ich möchte",
                        "German has a polite way of asking for something. 'Ich will' (I want) sounds "
                                + "blunt; 'ich möchte' (I would like) is what people actually say in a "
                                + "restaurant.",
                        List.of("Ich möchte einen Kaffee. — I would like a coffee.",
                                "The verb stays in second position: Ich | möchte | einen Kaffee.",
                                "Add 'bitte' at the end to sound friendlier.",
                                "For the bill: Die Rechnung, bitte."),
                        LEVEL, Skill.GRAMMAR, TOPIC),

                make.learnCard("Useful expressions",
                        "Four phrases that carry a whole restaurant visit.",
                        List.of(
                                make.example("Einen Tisch für zwei, bitte.", "A table for two, please."),
                                make.example("Ich möchte bestellen.", "I would like to order."),
                                make.example("Das schmeckt gut.", "That tastes good."),
                                make.example("Die Rechnung, bitte.", "The bill, please.")),
                        null, LEVEL, TOPIC),

                // -------------------------------------------------- CONTEXT
                make.dialogueScene("Im Restaurant",
                        "Anna arrives at a restaurant and orders lunch.",
                        List.of(
                                make.line("Kellner", "Guten Tag! Haben Sie reserviert?"),
                                make.line("Anna", "Nein. Einen Tisch für zwei, bitte."),
                                make.line("Kellner", "Gern. Was möchten Sie trinken?"),
                                make.line("Anna", "Ich möchte ein Wasser, bitte."),
                                make.line("Kellner", "Und zu essen?"),
                                make.line("Anna", "Die Suppe, bitte. Und dann die Rechnung.")),
                        LEVEL, TOPIC),

                make.realExamples("bestellen in real sentences",
                        "Sentences written by other learners and native speakers.",
                        "bestellen",
                        List.of(
                                make.example("Ich möchte einen Kaffee bestellen.",
                                        "I would like to order a coffee."),
                                make.example("Wir bestellen die Vorspeise später.",
                                        "We will order the starter later.")),
                        LEVEL, TOPIC),

                // ------------------------------------------- GUIDED PRACTICE
                make.choice(ActivityType.CONTEXT_CHOICE, ActivityPhase.GUIDED_PRACTICE,
                        "Choose what fits the situation",
                        "What do you say?",
                        "The waiter asks: „Was möchten Sie trinken?“",
                        List.of("Ich bin ein Wasser.", "Ich möchte ein Wasser, bitte.",
                                "Ich habe Wasser.", "Wasser ist gut."),
                        1,
                        "„Ich möchte …, bitte“ is the polite way to order anything.",
                        "Look for the polite form of 'wollen' from the grammar card.",
                        Skill.VOCABULARY, Difficulty.EASY, LEVEL, TOPIC),

                make.fillBlank(ActivityPhase.GUIDED_PRACTICE,
                        "Order a coffee politely",
                        "Ich ___ einen Kaffee, bitte.", "möchte",
                        "„Ich möchte“ = I would like. It is the polite form you just learned.",
                        "It is the polite form of 'wollen'.",
                        Skill.GRAMMAR, Difficulty.EASY, LEVEL, TOPIC),

                // ------------------------------------------------- PRACTICE
                make.sentenceBuilder(ActivityPhase.PRACTICE,
                        "Ask for the bill",
                        List.of("bitte", "Die", "Rechnung"), "Die Rechnung bitte",
                        "The noun comes first, then 'bitte' softens the request.",
                        null, Difficulty.EASY, LEVEL, TOPIC),

                make.fillBlank(ActivityPhase.PRACTICE,
                        "You have finished eating. What do you ask for?",
                        "Die ___, bitte.", "Rechnung",
                        "„Die Rechnung, bitte“ is how you ask to pay.",
                        null, Skill.VOCABULARY, Difficulty.EASY, LEVEL, TOPIC),

                make.translation(ActivityPhase.PRACTICE, "Translate into German",
                        "A table for two, please.", "Einen Tisch für zwei, bitte",
                        "„Einen Tisch“ is accusative — that is why 'der Tisch' becomes 'einen Tisch'.",
                        null, Difficulty.MEDIUM, LEVEL, TOPIC),

                make.choice(ActivityType.CONTEXT_CHOICE, ActivityPhase.PRACTICE,
                        "Choose what fits the situation",
                        "What do you say to the waiter?",
                        "You are ready to order but the waiter is walking past.",
                        List.of("Entschuldigung, ich möchte bestellen.", "Ich bin Kellner.",
                                "Die Speisekarte ist gut.", "Guten Morgen, bitte."),
                        0,
                        "„Entschuldigung“ gets attention politely, then you say what you want.",
                        null, Skill.READING, Difficulty.MEDIUM, LEVEL, TOPIC),

                // ---------------------------------------------------- APPLY
                make.shortWriting("Write two sentences to order food in a restaurant.",
                        "Imagine you are sitting at the table and the waiter is ready. "
                                + "Use „ich möchte“ and „bitte“.",
                        8, List.of("ich möchte", "bitte"),
                        "Anything like „Ich möchte die Suppe, bitte. Und ein Wasser, bitte.“ works.",
                        LEVEL, TOPIC),

                // ----------------------------------------------- CHECKPOINT
                make.checkpointCard("Checkpoint",
                        "Four tasks decide whether this lesson counts as done.", LEVEL),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.CHECKPOINT,
                        "Choose the correct answer",
                        "Which sentence is polite and correct?", null,
                        List.of("Ich will die Suppe.", "Ich möchte die Suppe, bitte.",
                                "Die Suppe ich möchte.", "Möchte ich die Suppe."),
                        1,
                        "Polite form plus the verb in second position.",
                        null, Skill.GRAMMAR, Difficulty.MEDIUM, LEVEL, TOPIC),

                make.matchPairs(ActivityPhase.CHECKPOINT,
                        "Match the German words with their meanings",
                        make.pairs(
                                "die Speisekarte", "the menu",
                                "die Rechnung", "the bill",
                                "bestellen", "to order",
                                "bezahlen", "to pay"),
                        "These four words appear in every restaurant visit.",
                        Skill.VOCABULARY, Difficulty.MEDIUM, LEVEL, TOPIC),

                make.fillBlank(ActivityPhase.CHECKPOINT,
                        "Complete the order",
                        "Ich möchte ___ Tisch für zwei.", "einen",
                        "'Tisch' is masculine and the object of the sentence, so it becomes „einen Tisch“.",
                        null, Skill.ARTICLES, Difficulty.HARD, LEVEL, TOPIC),

                make.dialogueChoice(ActivityPhase.CHECKPOINT,
                        List.of(make.line("Kellner", "Möchten Sie noch etwas trinken?")),
                        "How do you politely say no and ask to pay?",
                        List.of("Nein, danke. Die Rechnung, bitte.",
                                "Ja, ich bin die Rechnung.",
                                "Nein, ich möchte einen Tisch.",
                                "Danke, ich bestelle einen Kellner."),
                        0,
                        "„Nein, danke“ plus „Die Rechnung, bitte“ closes the visit politely.",
                        null, Difficulty.MEDIUM, LEVEL, TOPIC));
    }
}
