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
 * A1 — the original basics course: greetings, introductions, numbers, articles
 * and the two verbs everything is built on.
 */
@Component
public class A1BasicsContent extends CourseContent {

    private static final CefrLevel LEVEL = CefrLevel.A1;

    @Override
    public String courseName() {
        return "German A1";
    }

    @Override
    public int order() {
        return 10;
    }

    @Override
    public void seed() {
        Course course = course(courseName(), LEVEL,
                "Learn basic German vocabulary and grammar for beginners");

        Unit basics = unit(course, "Basics", 0, "Learn essential greetings and introductions");
        Unit sentences = unit(course, "Simple Sentences", 1, "Build your first German sentences");

        greetings(basics);
        introductions(basics);
        numbers(basics);
        articles(sentences);
        verbs(sentences);
    }

    private void greetings(Unit unit) {
        Lesson lesson = lesson(unit, "Greetings", 0, "Learn basic German greetings", LEVEL);
        String topic = "greetings";

        add(lesson,
                make.vocabulary("Hallo!", "German greets differently depending on the time of day.",
                        List.of(
                                make.word("Hallo", "Hello"),
                                make.word("Guten Morgen", "Good morning"),
                                make.word("Guten Tag", "Good day"),
                                make.word("Gute Nacht", "Good night"),
                                make.word("Tschüss", "Bye")),
                        LEVEL, topic),

                make.learnCard("Which one when?", "Pick the greeting that matches the time of day.",
                        null,
                        List.of("Guten Morgen — until about 11 in the morning",
                                "Guten Tag — during the day",
                                "Guten Abend — in the evening",
                                "Gute Nacht — only when going to bed"),
                        LEVEL, topic),

                make.dialogueScene("Kurz und freundlich", "Two neighbours meet in the morning.",
                        List.of(
                                make.line("Anna", "Guten Morgen!"),
                                make.line("Herr Weber", "Guten Morgen, Anna. Wie geht es Ihnen?"),
                                make.line("Anna", "Danke, gut. Tschüss!")),
                        LEVEL, topic),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.GUIDED_PRACTICE,
                        "Choose the correct answer", "What does 'Hallo' mean?", null,
                        List.of("Goodbye", "Hello", "Thank you", "Please"), 1,
                        "'Hallo' is the German word for 'Hello'",
                        "It is the one you can use at any time of day.",
                        Skill.VOCABULARY, Difficulty.EASY, LEVEL, topic),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.PRACTICE,
                        "Choose the correct answer", "What does 'Tschüss' mean?", null,
                        List.of("Hello", "Good morning", "Goodbye", "Good night"), 2,
                        "'Tschüss' is an informal way to say 'Goodbye' in German",
                        null, Skill.VOCABULARY, Difficulty.EASY, LEVEL, topic),

                make.choice(ActivityType.CONTEXT_CHOICE, ActivityPhase.PRACTICE,
                        "Choose what fits the situation", "What do you say?",
                        "It is 8 in the morning and you meet your neighbour on the stairs.",
                        List.of("Guten Abend", "Guten Morgen", "Gute Nacht", "Tschüss"), 1,
                        "Before about 11 in the morning Germans say 'Guten Morgen'",
                        null, Skill.READING, Difficulty.MEDIUM, LEVEL, topic),

                make.fillBlank(ActivityPhase.PRACTICE, "Complete: Good morning in German",
                        "___ (Good morning)", "Guten Morgen",
                        "'Guten Morgen' means 'Good morning'",
                        null, Skill.VOCABULARY, Difficulty.EASY, LEVEL, topic),

                make.listening(ActivityPhase.PRACTICE, "Guten Morgen",
                        List.of("Good morning", "Good night", "Goodbye", "Hello"), 0,
                        "'Guten Morgen' is what you just heard",
                        Difficulty.EASY, LEVEL, topic),

                make.checkpointCard("Checkpoint", "Three tasks decide whether this lesson is done.", LEVEL),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.CHECKPOINT,
                        "Choose the correct answer", "What does 'Guten Tag' mean?", null,
                        List.of("Good night", "Good day", "Good morning", "Goodbye"), 1,
                        "'Guten Tag' is a formal greeting meaning 'Good day'",
                        null, Skill.VOCABULARY, Difficulty.EASY, LEVEL, topic),

                make.matchPairs(ActivityPhase.CHECKPOINT,
                        "Match the German greetings with their English meanings",
                        make.pairs("Hallo", "Hello", "Danke", "Thank you", "Tschüss", "Goodbye"),
                        "These are common German greetings and expressions",
                        Skill.VOCABULARY, Difficulty.MEDIUM, LEVEL, topic),

                make.fillBlank(ActivityPhase.CHECKPOINT, "How do you say 'Good night' in German?",
                        "___ (Good night)", "Gute Nacht",
                        "'Gute Nacht' means 'Good night'",
                        null, Skill.VOCABULARY, Difficulty.MEDIUM, LEVEL, topic));
    }

    private void introductions(Unit unit) {
        Lesson lesson = lesson(unit, "Introducing Yourself", 1,
                "Learn to introduce yourself in German", LEVEL);
        String topic = "introductions";

        add(lesson,
                make.vocabulary("Ich heiße …", "German uses 'heißen' (to be called) to give your name.",
                        List.of(
                                make.word("Ich heiße …", "My name is …"),
                                make.word("Ich komme aus …", "I come from …"),
                                make.word("Freut mich", "Nice to meet you"),
                                make.word("Wie heißt du?", "What is your name? (informal)")),
                        LEVEL, topic),

                make.learnCard("Asking back", "To ask someone informally, turn the verb around.",
                        null,
                        List.of("Wie heißt du? — What is your name? (informal)",
                                "Wie heißen Sie? — What is your name? (formal)",
                                "Woher kommst du? — Where do you come from?"),
                        LEVEL, topic),

                make.fillBlank(ActivityPhase.GUIDED_PRACTICE, "Complete: My name is Max",
                        "Ich ___ Max.", "heiße|heisse",
                        "'Ich heiße...' means 'My name is...' or literally 'I am called...'",
                        "It comes from the verb 'heißen'.",
                        Skill.VOCABULARY, Difficulty.EASY, LEVEL, topic),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.PRACTICE,
                        "Choose the correct answer", "What does 'Wie heißt du?' mean?", null,
                        List.of("How are you?", "What is your name?", "Where are you from?",
                                "How old are you?"), 1,
                        "'Wie heißt du?' is the informal way to ask someone's name",
                        null, Skill.VOCABULARY, Difficulty.EASY, LEVEL, topic),

                make.sentenceBuilder(ActivityPhase.PRACTICE, "Arrange to say: My name is Anna",
                        List.of("heiße", "Anna", "Ich"), "Ich heiße Anna",
                        "The correct word order is: Ich heiße Anna",
                        null, Difficulty.MEDIUM, LEVEL, topic),

                make.translation(ActivityPhase.PRACTICE, "Translate into German",
                        "I come from Germany.", "Ich komme aus Deutschland",
                        "'Ich komme aus...' means 'I come from...'",
                        null, Difficulty.MEDIUM, LEVEL, topic),

                make.checkpointCard("Checkpoint", "Show that you can introduce yourself.", LEVEL),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.CHECKPOINT,
                        "Choose the correct answer",
                        "How do you say 'Nice to meet you' in German?", null,
                        List.of("Danke schön", "Freut mich", "Bitte schön", "Entschuldigung"), 1,
                        "'Freut mich' literally means 'Pleases me' and is used for 'Nice to meet you'",
                        null, Skill.VOCABULARY, Difficulty.MEDIUM, LEVEL, topic),

                make.dialogueChoice(ActivityPhase.CHECKPOINT,
                        List.of(make.line("Anna", "Hallo! Wie heißt du?")),
                        "How do you reply?",
                        List.of("Ich heiße Max.", "Gute Nacht.", "Ich komme.", "Tschüss!"), 0,
                        "Answer the question with 'Ich heiße' and your name",
                        null, Difficulty.MEDIUM, LEVEL, topic),

                make.matchPairs(ActivityPhase.CHECKPOINT,
                        "Match the German phrases with their meanings",
                        make.pairs("Ich heiße", "My name is", "Ich komme aus", "I come from",
                                "Freut mich", "Nice to meet you"),
                        "These phrases are essential for introducing yourself",
                        Skill.VOCABULARY, Difficulty.MEDIUM, LEVEL, topic));
    }

    private void numbers(Unit unit) {
        Lesson lesson = lesson(unit, "Numbers 1-10", 2, "Learn to count in German", LEVEL);
        String topic = "numbers";

        add(lesson,
                make.vocabulary("Eins bis zehn", "The first ten numbers build every larger number.",
                        List.of(
                                make.word("eins, zwei, drei", "one, two, three"),
                                make.word("vier, fünf, sechs", "four, five, six"),
                                make.word("sieben, acht", "seven, eight"),
                                make.word("neun, zehn", "nine, ten")),
                        LEVEL, topic),

                make.matchPairs(ActivityPhase.GUIDED_PRACTICE, "Match the German numbers 1-3",
                        make.pairs("eins", "1", "zwei", "2", "drei", "3"),
                        "eins=1, zwei=2, drei=3",
                        Skill.VOCABULARY, Difficulty.EASY, LEVEL, topic),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.PRACTICE,
                        "Choose the correct answer", "What is 'vier' in English?", null,
                        List.of("five", "four", "three", "six"), 1,
                        "'Vier' means 'four' in German",
                        null, Skill.VOCABULARY, Difficulty.EASY, LEVEL, topic),

                make.fillBlank(ActivityPhase.PRACTICE, "What comes after vier?",
                        "vier, ___, sechs", "fünf|funf",
                        "'Fünf' (5) comes after 'vier' (4)",
                        null, Skill.VOCABULARY, Difficulty.EASY, LEVEL, topic),

                make.listening(ActivityPhase.PRACTICE, "sieben",
                        List.of("six", "seven", "eight", "nine"), 1, "'Sieben' is 7",
                        Difficulty.MEDIUM, LEVEL, topic),

                make.checkpointCard("Checkpoint", "Count from six to ten without help.", LEVEL),

                make.matchPairs(ActivityPhase.CHECKPOINT, "Match the German numbers 6-8",
                        make.pairs("sechs", "6", "sieben", "7", "acht", "8"),
                        "sechs=6, sieben=7, acht=8",
                        Skill.VOCABULARY, Difficulty.MEDIUM, LEVEL, topic),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.CHECKPOINT,
                        "Choose the correct answer", "What is 'neun' in English?", null,
                        List.of("eight", "ten", "nine", "seven"), 2,
                        "'Neun' means 'nine' in German",
                        null, Skill.VOCABULARY, Difficulty.EASY, LEVEL, topic),

                make.fillBlank(ActivityPhase.CHECKPOINT, "What is 10 in German?",
                        "acht, neun, ___", "zehn", "'Zehn' means 'ten' in German",
                        null, Skill.VOCABULARY, Difficulty.MEDIUM, LEVEL, topic));
    }

    private void articles(Unit unit) {
        Lesson lesson = lesson(unit, "Articles", 0, "Learn German articles: der, die, das", LEVEL);
        String topic = "articles";

        add(lesson,
                make.grammarTip("der, die, das",
                        "Every German noun has a gender, and the article shows it.",
                        List.of("der Mann — masculine", "die Frau — feminine", "das Kind — neuter",
                                "Learn the article together with the noun, never on its own."),
                        LEVEL, Skill.ARTICLES, topic),

                make.learnCard("Patterns that help",
                        "There is no full rule, but some endings are reliable.",
                        null,
                        List.of("-ung, -heit, -keit are almost always 'die'",
                                "-chen is always 'das'",
                                "-er for people is usually 'der'"),
                        LEVEL, topic),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.GUIDED_PRACTICE,
                        "Choose the correct article", "Which article goes with 'Mann' (man)?", null,
                        List.of("der", "die", "das"), 0,
                        "'Der Mann' - masculine nouns use 'der'",
                        "A man is masculine.",
                        Skill.ARTICLES, Difficulty.EASY, LEVEL, topic),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.PRACTICE,
                        "Choose the correct article", "Which article goes with 'Frau' (woman)?", null,
                        List.of("der", "die", "das"), 1,
                        "'Die Frau' - feminine nouns use 'die'",
                        null, Skill.ARTICLES, Difficulty.EASY, LEVEL, topic),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.PRACTICE,
                        "Choose the correct article", "Which article goes with 'Kind' (child)?", null,
                        List.of("der", "die", "das"), 2,
                        "'Das Kind' - neuter nouns use 'das'",
                        null, Skill.ARTICLES, Difficulty.EASY, LEVEL, topic),

                make.fillBlank(ActivityPhase.PRACTICE, "Complete: The book (neuter)",
                        "___ Buch", "das", "'Buch' is neuter, so it uses 'das'",
                        null, Skill.ARTICLES, Difficulty.MEDIUM, LEVEL, topic),

                make.checkpointCard("Checkpoint", "Three tasks on genders.", LEVEL),

                make.matchPairs(ActivityPhase.CHECKPOINT, "Match the nouns with their articles",
                        make.pairs("der Hund", "the dog (m)", "die Katze", "the cat (f)",
                                "das Haus", "the house (n)"),
                        "der=masculine, die=feminine, das=neuter",
                        Skill.ARTICLES, Difficulty.MEDIUM, LEVEL, topic),

                make.fillBlank(ActivityPhase.CHECKPOINT, "Complete: The apple (masculine)",
                        "___ Apfel", "der", "'Apfel' is masculine, so it uses 'der'",
                        null, Skill.ARTICLES, Difficulty.MEDIUM, LEVEL, topic),

                make.choice(ActivityType.CONTEXT_CHOICE, ActivityPhase.CHECKPOINT,
                        "Choose what fits the situation", "Which article belongs in the gap?",
                        "You are pointing at a house: ___ Haus ist groß.",
                        List.of("Der", "Die", "Das"), 2,
                        "'Haus' is neuter, so it takes 'das'",
                        null, Skill.ARTICLES, Difficulty.HARD, LEVEL, topic));
    }

    private void verbs(Unit unit) {
        Lesson lesson = lesson(unit, "Simple Verbs", 1,
                "Learn sein (to be) and haben (to have)", LEVEL);
        String topic = "verbs";

        add(lesson,
                make.grammarTip("sein — to be",
                        "The most important German verb, and an irregular one.",
                        List.of("ich bin — I am", "du bist — you are",
                                "er/sie/es ist — he/she/it is", "wir sind — we are"),
                        LEVEL, Skill.VERB_CONJUGATION, topic),

                make.grammarTip("haben — to have",
                        "The second verb you need for almost every sentence.",
                        List.of("ich habe — I have", "du hast — you have",
                                "er/sie/es hat — he/she/it has", "sie haben — they have"),
                        LEVEL, Skill.VERB_CONJUGATION, topic),

                make.fillBlank(ActivityPhase.GUIDED_PRACTICE, "Complete: I am tired",
                        "Ich ___ müde.", "bin",
                        "'Ich bin' = 'I am'. 'Sein' conjugates to 'bin' for 'ich'",
                        "Look at the first line of the 'sein' card.",
                        Skill.VERB_CONJUGATION, Difficulty.EASY, LEVEL, topic),

                make.choice(ActivityType.MULTIPLE_CHOICE, ActivityPhase.PRACTICE,
                        "Choose the correct answer",
                        "How do you say 'You are' (informal) in German?", null,
                        List.of("Ich bin", "Du bist", "Er ist", "Wir sind"), 1,
                        "'Du bist' = 'You are' (informal singular)",
                        null, Skill.VERB_CONJUGATION, Difficulty.EASY, LEVEL, topic),

                make.fillBlank(ActivityPhase.PRACTICE, "Complete: He has a car",
                        "Er ___ ein Auto.", "hat",
                        "'Er hat' = 'He has'. 'Haben' conjugates to 'hat' for 'er/sie/es'",
                        null, Skill.VERB_CONJUGATION, Difficulty.MEDIUM, LEVEL, topic),

                make.shortWriting("Write one sentence about yourself",
                        "Use 'ich bin' and say how you feel today.",
                        3, List.of("ich", "bin"),
                        "Anything like 'Ich bin müde' or 'Ich bin glücklich' works",
                        LEVEL, topic),

                make.checkpointCard("Checkpoint", "Put the two verbs to work.", LEVEL),

                make.sentenceBuilder(ActivityPhase.CHECKPOINT, "Arrange: We are happy",
                        List.of("glücklich", "Wir", "sind"), "Wir sind glücklich",
                        "'Wir sind' = 'We are'",
                        null, Difficulty.MEDIUM, LEVEL, topic),

                make.matchPairs(ActivityPhase.CHECKPOINT,
                        "Match the verb forms with their meanings",
                        make.pairs("ich bin", "I am", "du hast", "you have", "er ist", "he is"),
                        "'sein' (to be) and 'haben' (to have) are the most important German verbs",
                        Skill.VERB_CONJUGATION, Difficulty.MEDIUM, LEVEL, topic),

                make.fillBlank(ActivityPhase.CHECKPOINT, "Complete: They have time",
                        "Sie ___ Zeit.", "haben",
                        "'Sie haben' = 'They have' (or formal 'You have')",
                        null, Skill.VERB_CONJUGATION, Difficulty.MEDIUM, LEVEL, topic));
    }
}
