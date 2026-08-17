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
 * B2 — arguing about an abstract topic.
 *
 * The same restaurant world, but now discussed rather than experienced:
 * passive voice, nominalisation and the connectors of a written argument.
 */
@Component
public class B2GastronomyContent extends CourseContent {

    private static final CefrLevel LEVEL = CefrLevel.B2;
    private static final String TOPIC = "sustainability";

    @Override
    public String courseName() {
        return "German B2";
    }

    @Override
    public int order() {
        return 50;
    }

    @Override
    public void seed() {
        Course course = course(courseName(), LEVEL,
                "Discuss abstract topics, weigh arguments and write formally");
        Unit unit = unit(course, "Gesellschaft und Debatte", 0, "Topics you argue about, not just describe");
        Lesson lesson = lesson(unit, "Gastronomie und Nachhaltigkeit", 0,
                "Discuss sustainable food: passive voice and written argument", LEVEL);

        add(lesson,
                // ---------------------------------------------------- LEARN
                make.vocabulary("Wortschatz: Nachhaltigkeit",
                        "Abstract nouns are the backbone of B2 discussion.",
                        List.of(
                                make.word("die Nachhaltigkeit", "sustainability"),
                                make.word("die Lieferkette", "the supply chain"),
                                make.word("der Anbau", "cultivation, growing"),
                                make.word("die Verschwendung", "waste (of resources)"),
                                make.word("verschwenden", "to waste"),
                                make.word("saisonal", "seasonal"),
                                make.word("der Verbraucher", "the consumer"),
                                make.word("die Verpackung", "the packaging")),
                        LEVEL, TOPIC),

                make.grammarTip("Das Passiv: what happens, not who does it",
                        "Formal German often leaves the actor out and puts the process first. "
                                + "The passive is werden plus the participle.",
                        List.of("Das Gemüse wird regional angebaut. — The vegetables are grown regionally.",
                                "Lebensmittel werden täglich weggeworfen. — Food is thrown away daily.",
                                "Past: Die Verpackung wurde reduziert. — The packaging was reduced.",
                                "Add the actor only if it matters: … wird von den Gästen bestellt."),
                        LEVEL, Skill.GRAMMAR, TOPIC),

                make.learnCard("Einen Standpunkt aufbauen",
                        "In a written argument, the connectors do the structuring work.",
                        null,
                        List.of("einerseits … andererseits — on the one hand … on the other",
                                "dennoch — nevertheless",
                                "folglich — consequently",
                                "im Gegensatz dazu — in contrast to this",
                                "zusammenfassend — to summarise"),
                        LEVEL, TOPIC),

                // -------------------------------------------------- CONTEXT
                make.dialogueScene("Debatte in der Gastronomie",
                        "A restaurant owner and a supplier disagree about regional sourcing.",
                        List.of(
                                make.line("Wirtin", "Einerseits möchten die Gäste regionale Produkte, "
                                        + "andererseits sollen die Preise niedrig bleiben."),
                                make.line("Lieferant", "Regionale Ware wird oft teurer eingekauft, "
                                        + "das stimmt."),
                                make.line("Wirtin", "Dennoch wird bei uns kaum etwas weggeworfen."),
                                make.line("Lieferant", "Folglich sparen Sie an anderer Stelle."),
                                make.line("Wirtin", "Genau. Verschwendung ist teurer als guter Einkauf.")),
                        LEVEL, TOPIC),

                make.realExamples("„Nachhaltigkeit“ in real sentences",
                        "The word as it is actually used.",
                        "Nachhaltigkeit",
                        List.of(
                                make.example("Nachhaltigkeit ist für viele Betriebe wichtig geworden.",
                                        "Sustainability has become important for many businesses."),
                                make.example("Über Nachhaltigkeit wird viel diskutiert.",
                                        "There is a lot of discussion about sustainability.")),
                        LEVEL, TOPIC),

                // ------------------------------------------- GUIDED PRACTICE
                make.fillBlank(ActivityPhase.GUIDED_PRACTICE,
                        "Complete the passive sentence",
                        "Das Gemüse ___ regional angebaut.", "wird",
                        "Passive present: werden in second position, participle at the end.",
                        "The passive is built with a form of 'werden'.",
                        Skill.GRAMMAR, Difficulty.MEDIUM, LEVEL, TOPIC),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.GUIDED_PRACTICE,
                        "Choose the correct passive form",
                        "Which sentence is correct passive in the past?", null,
                        List.of("Die Verpackung ist reduziert worden von uns.",
                                "Die Verpackung wurde reduziert.",
                                "Die Verpackung hat reduziert.",
                                "Die Verpackung wird reduziert haben."),
                        1,
                        "„wurde + Partizip II“ is the straightforward past passive.",
                        "Look for 'wurde' plus a participle.",
                        Skill.VERB_CONJUGATION, Difficulty.HARD, LEVEL, TOPIC),

                // ------------------------------------------------- PRACTICE
                make.sentenceBuilder(ActivityPhase.PRACTICE,
                        "Build: The food is bought regionally.",
                        List.of("eingekauft", "Die", "regional", "Lebensmittel", "werden"),
                        "Die Lebensmittel werden regional eingekauft",
                        "„werden“ takes second position, the participle closes the sentence.",
                        null, Difficulty.HARD, LEVEL, TOPIC),

                make.translation(ActivityPhase.PRACTICE, "Translate into German",
                        "Food waste is a big problem.",
                        "Lebensmittelverschwendung ist ein großes Problem",
                        "German prefers one compound noun where English uses two words.",
                        null, Difficulty.HARD, LEVEL, TOPIC),

                make.choice(ActivityType.CONTEXT_CHOICE, ActivityPhase.PRACTICE,
                        "Choose the sentence that fits a formal text",
                        "Which version belongs in a written argument?",
                        "You are writing an article about restaurants and sustainability.",
                        List.of("Restaurants schmeißen echt viel weg, finde ich.",
                                "In der Gastronomie werden täglich große Mengen an Lebensmitteln "
                                        + "verschwendet.",
                                "Die werfen einfach alles weg.",
                                "Essen wegwerfen ist doof."),
                        1,
                        "Passive plus abstract nouns is what makes the register formal.",
                        null, Skill.WRITING, Difficulty.HARD, LEVEL, TOPIC),

                // ---------------------------------------------------- APPLY
                make.shortWriting("Nehmen Sie Stellung.",
                        "Should restaurants be obliged to buy regionally? Write a short argument "
                                + "with both sides. Use „einerseits“ and at least one passive sentence.",
                        30, List.of("einerseits"),
                        "For example: „Einerseits wird regionale Ware teurer eingekauft, "
                                + "andererseits werden dadurch lange Lieferketten vermieden. "
                                + "Folglich profitieren langfristig beide Seiten.“",
                        LEVEL, TOPIC),

                // ----------------------------------------------- CHECKPOINT
                make.checkpointCard("Checkpoint",
                        "Three tasks on passive voice and formal register.", LEVEL),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.CHECKPOINT,
                        "Choose the correct answer",
                        "Which sentence is in the passive?", null,
                        List.of("Der Koch kauft das Gemüse regional ein.",
                                "Das Gemüse wird regional eingekauft.",
                                "Der Koch hat regional eingekauft.",
                                "Das Gemüse kauft regional ein."),
                        1,
                        "„wird … eingekauft“ — werden plus participle, actor left out.",
                        null, Skill.GRAMMAR, Difficulty.HARD, LEVEL, TOPIC),

                make.fillBlank(ActivityPhase.CHECKPOINT,
                        "Nominalise the verb: „verschwenden“ becomes …",
                        "Die ___ von Lebensmitteln ist ein großes Problem.", "Verschwendung",
                        "Nominalisation turns the action into a noun, which formal German prefers.",
                        null, Skill.VOCABULARY, Difficulty.HARD, LEVEL, TOPIC),

                make.matchPairs(ActivityPhase.CHECKPOINT,
                        "Match the connectors with their function",
                        make.pairs(
                                "einerseits", "introduces the first side",
                                "dennoch", "concedes but disagrees",
                                "folglich", "draws a conclusion",
                                "zusammenfassend", "sums the argument up"),
                        "Knowing what each connector signals is what makes an argument readable.",
                        Skill.WRITING, Difficulty.HARD, LEVEL, TOPIC));
    }
}
