package com.germanlearning.config;

import com.germanlearning.model.*;
import com.germanlearning.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashMap;
import java.util.Map;

@Component
public class DataInitializer implements CommandLineRunner {

    private final CourseRepository courseRepository;
    private final UnitRepository unitRepository;
    private final LessonRepository lessonRepository;
    private final ExerciseRepository exerciseRepository;

    public DataInitializer(CourseRepository courseRepository,
                          UnitRepository unitRepository,
                          LessonRepository lessonRepository,
                          ExerciseRepository exerciseRepository) {
        this.courseRepository = courseRepository;
        this.unitRepository = unitRepository;
        this.lessonRepository = lessonRepository;
        this.exerciseRepository = exerciseRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (courseRepository.count() > 0) {
            return;
        }

        Course germanA1 = new Course("German A1", "German", "A1", 
                "Learn basic German vocabulary and grammar for beginners");
        courseRepository.save(germanA1);

        Unit unit1 = new Unit("Basics", 0, "Learn essential greetings and introductions");
        germanA1.addUnit(unit1);
        unitRepository.save(unit1);

        Unit unit2 = new Unit("Simple Sentences", 1, "Build your first German sentences");
        germanA1.addUnit(unit2);
        unitRepository.save(unit2);

        createLesson1Greetings(unit1);
        createLesson2Introductions(unit1);
        createLesson3Numbers(unit1);
        createLesson4Articles(unit2);
        createLesson5Verbs(unit2);
    }

    private void createLesson1Greetings(Unit unit) {
        Lesson lesson = new Lesson("Greetings", 0, "Learn basic German greetings");
        unit.addLesson(lesson);
        lessonRepository.save(lesson);

        MultipleChoiceExercise ex1 = new MultipleChoiceExercise();
        ex1.setQuestion("What does 'Hallo' mean?");
        ex1.setCorrectAnswer("Hello");
        ex1.setOptionsFromArray(new String[]{"Goodbye", "Hello", "Thank you", "Please"});
        ex1.setCorrectOptionIndex(1);
        ex1.setExplanation("'Hallo' is the German word for 'Hello'");
        lesson.addExercise(ex1);
        exerciseRepository.save(ex1);

        MultipleChoiceExercise ex2 = new MultipleChoiceExercise();
        ex2.setQuestion("What does 'Tschuss' mean?");
        ex2.setCorrectAnswer("Goodbye");
        ex2.setOptionsFromArray(new String[]{"Hello", "Good morning", "Goodbye", "Good night"});
        ex2.setCorrectOptionIndex(2);
        ex2.setExplanation("'Tschuss' is an informal way to say 'Goodbye' in German");
        lesson.addExercise(ex2);
        exerciseRepository.save(ex2);

        FillBlankExercise ex3 = new FillBlankExercise();
        ex3.setQuestion("Complete: Good morning in German");
        ex3.setCorrectAnswer("Guten Morgen");
        ex3.setSentenceTemplate("___ (Good morning)");
        ex3.setExplanation("'Guten Morgen' means 'Good morning'");
        lesson.addExercise(ex3);
        exerciseRepository.save(ex3);

        MultipleChoiceExercise ex4 = new MultipleChoiceExercise();
        ex4.setQuestion("What does 'Guten Tag' mean?");
        ex4.setCorrectAnswer("Good day");
        ex4.setOptionsFromArray(new String[]{"Good night", "Good day", "Good morning", "Goodbye"});
        ex4.setCorrectOptionIndex(1);
        ex4.setExplanation("'Guten Tag' is a formal greeting meaning 'Good day'");
        lesson.addExercise(ex4);
        exerciseRepository.save(ex4);

        Map<String, String> pairs1 = new HashMap<>();
        pairs1.put("Hallo", "Hello");
        pairs1.put("Tschuss", "Goodbye");
        pairs1.put("Danke", "Thank you");
        MatchExercise ex5 = new MatchExercise();
        ex5.setQuestion("Match the German greetings with their English meanings");
        ex5.setCorrectAnswer("Hallo:Hello;Tschuss:Goodbye;Danke:Thank you");
        ex5.setPairs(pairs1);
        ex5.setExplanation("These are common German greetings and expressions");
        lesson.addExercise(ex5);
        exerciseRepository.save(ex5);

        FillBlankExercise ex6 = new FillBlankExercise();
        ex6.setQuestion("How do you say 'Good night' in German?");
        ex6.setCorrectAnswer("Gute Nacht");
        ex6.setSentenceTemplate("___ (Good night)");
        ex6.setExplanation("'Gute Nacht' means 'Good night'");
        lesson.addExercise(ex6);
        exerciseRepository.save(ex6);
    }

    private void createLesson2Introductions(Unit unit) {
        Lesson lesson = new Lesson("Introducing Yourself", 1, "Learn to introduce yourself in German");
        unit.addLesson(lesson);
        lessonRepository.save(lesson);

        FillBlankExercise ex1 = new FillBlankExercise();
        ex1.setQuestion("Complete: My name is Max");
        ex1.setCorrectAnswer("heiße|heisse");
        ex1.setSentenceTemplate("Ich ___ Max.");
        ex1.setExplanation("'Ich heiße...' means 'My name is...' or literally 'I am called...'");
        lesson.addExercise(ex1);
        exerciseRepository.save(ex1);

        MultipleChoiceExercise ex2 = new MultipleChoiceExercise();
        ex2.setQuestion("What does 'Wie heißt du?' mean?");
        ex2.setCorrectAnswer("What is your name?");
        ex2.setOptionsFromArray(new String[]{"How are you?", "What is your name?", "Where are you from?", "How old are you?"});
        ex2.setCorrectOptionIndex(1);
        ex2.setExplanation("'Wie heißt du?' is the informal way to ask someone's name");
        lesson.addExercise(ex2);
        exerciseRepository.save(ex2);

        OrderExercise ex3 = new OrderExercise();
        ex3.setQuestion("Arrange to say: My name is Anna");
        ex3.setCorrectAnswer("Ich heiße Anna");
        ex3.setCorrectOrder("Ich heiße Anna");
        ex3.setShuffledWordsFromList(java.util.List.of("Anna", "heiße", "Ich"));
        ex3.setExplanation("The correct word order is: Ich heiße Anna");
        lesson.addExercise(ex3);
        exerciseRepository.save(ex3);

        FillBlankExercise ex4 = new FillBlankExercise();
        ex4.setQuestion("Complete: I come from Germany");
        ex4.setCorrectAnswer("komme");
        ex4.setSentenceTemplate("Ich ___ aus Deutschland.");
        ex4.setExplanation("'Ich komme aus...' means 'I come from...'");
        lesson.addExercise(ex4);
        exerciseRepository.save(ex4);

        MultipleChoiceExercise ex5 = new MultipleChoiceExercise();
        ex5.setQuestion("How do you say 'Nice to meet you' in German?");
        ex5.setCorrectAnswer("Freut mich");
        ex5.setOptionsFromArray(new String[]{"Danke schön", "Freut mich", "Bitte schön", "Entschuldigung"});
        ex5.setCorrectOptionIndex(1);
        ex5.setExplanation("'Freut mich' literally means 'Pleases me' and is used for 'Nice to meet you'");
        lesson.addExercise(ex5);
        exerciseRepository.save(ex5);

        Map<String, String> pairs = new HashMap<>();
        pairs.put("Ich heiße", "My name is");
        pairs.put("Ich komme aus", "I come from");
        pairs.put("Freut mich", "Nice to meet you");
        MatchExercise ex6 = new MatchExercise();
        ex6.setQuestion("Match the German phrases with their meanings");
        ex6.setCorrectAnswer("Ich heiße:My name is;Ich komme aus:I come from;Freut mich:Nice to meet you");
        ex6.setPairs(pairs);
        ex6.setExplanation("These phrases are essential for introducing yourself");
        lesson.addExercise(ex6);
        exerciseRepository.save(ex6);
    }

    private void createLesson3Numbers(Unit unit) {
        Lesson lesson = new Lesson("Numbers 1-10", 2, "Learn to count in German");
        unit.addLesson(lesson);
        lessonRepository.save(lesson);

        Map<String, String> pairs1 = new HashMap<>();
        pairs1.put("eins", "1");
        pairs1.put("zwei", "2");
        pairs1.put("drei", "3");
        MatchExercise ex1 = new MatchExercise();
        ex1.setQuestion("Match the German numbers 1-3");
        ex1.setCorrectAnswer("eins:1;zwei:2;drei:3");
        ex1.setPairs(pairs1);
        ex1.setExplanation("eins=1, zwei=2, drei=3");
        lesson.addExercise(ex1);
        exerciseRepository.save(ex1);

        MultipleChoiceExercise ex2 = new MultipleChoiceExercise();
        ex2.setQuestion("What is 'vier' in English?");
        ex2.setCorrectAnswer("four");
        ex2.setOptionsFromArray(new String[]{"five", "four", "three", "six"});
        ex2.setCorrectOptionIndex(1);
        ex2.setExplanation("'Vier' means 'four' in German");
        lesson.addExercise(ex2);
        exerciseRepository.save(ex2);

        FillBlankExercise ex3 = new FillBlankExercise();
        ex3.setQuestion("What comes after vier?");
        ex3.setCorrectAnswer("fünf|funf");
        ex3.setSentenceTemplate("vier, ___, sechs");
        ex3.setExplanation("'Fünf' (5) comes after 'vier' (4)");
        lesson.addExercise(ex3);
        exerciseRepository.save(ex3);

        Map<String, String> pairs2 = new HashMap<>();
        pairs2.put("sechs", "6");
        pairs2.put("sieben", "7");
        pairs2.put("acht", "8");
        MatchExercise ex4 = new MatchExercise();
        ex4.setQuestion("Match the German numbers 6-8");
        ex4.setCorrectAnswer("sechs:6;sieben:7;acht:8");
        ex4.setPairs(pairs2);
        ex4.setExplanation("sechs=6, sieben=7, acht=8");
        lesson.addExercise(ex4);
        exerciseRepository.save(ex4);

        MultipleChoiceExercise ex5 = new MultipleChoiceExercise();
        ex5.setQuestion("What is 'neun' in English?");
        ex5.setCorrectAnswer("nine");
        ex5.setOptionsFromArray(new String[]{"eight", "ten", "nine", "seven"});
        ex5.setCorrectOptionIndex(2);
        ex5.setExplanation("'Neun' means 'nine' in German");
        lesson.addExercise(ex5);
        exerciseRepository.save(ex5);

        FillBlankExercise ex6 = new FillBlankExercise();
        ex6.setQuestion("What is 10 in German?");
        ex6.setCorrectAnswer("zehn");
        ex6.setSentenceTemplate("acht, neun, ___");
        ex6.setExplanation("'Zehn' means 'ten' in German");
        lesson.addExercise(ex6);
        exerciseRepository.save(ex6);
    }

    private void createLesson4Articles(Unit unit) {
        Lesson lesson = new Lesson("Articles", 0, "Learn German articles: der, die, das");
        unit.addLesson(lesson);
        lessonRepository.save(lesson);

        MultipleChoiceExercise ex1 = new MultipleChoiceExercise();
        ex1.setQuestion("Which article goes with 'Mann' (man)?");
        ex1.setCorrectAnswer("der");
        ex1.setOptionsFromArray(new String[]{"der", "die", "das"});
        ex1.setCorrectOptionIndex(0);
        ex1.setExplanation("'Der Mann' - masculine nouns use 'der'");
        lesson.addExercise(ex1);
        exerciseRepository.save(ex1);

        MultipleChoiceExercise ex2 = new MultipleChoiceExercise();
        ex2.setQuestion("Which article goes with 'Frau' (woman)?");
        ex2.setCorrectAnswer("die");
        ex2.setOptionsFromArray(new String[]{"der", "die", "das"});
        ex2.setCorrectOptionIndex(1);
        ex2.setExplanation("'Die Frau' - feminine nouns use 'die'");
        lesson.addExercise(ex2);
        exerciseRepository.save(ex2);

        MultipleChoiceExercise ex3 = new MultipleChoiceExercise();
        ex3.setQuestion("Which article goes with 'Kind' (child)?");
        ex3.setCorrectAnswer("das");
        ex3.setOptionsFromArray(new String[]{"der", "die", "das"});
        ex3.setCorrectOptionIndex(2);
        ex3.setExplanation("'Das Kind' - neuter nouns use 'das'");
        lesson.addExercise(ex3);
        exerciseRepository.save(ex3);

        FillBlankExercise ex4 = new FillBlankExercise();
        ex4.setQuestion("Complete: The book (neuter)");
        ex4.setCorrectAnswer("das");
        ex4.setSentenceTemplate("___ Buch");
        ex4.setExplanation("'Buch' is neuter, so it uses 'das'");
        lesson.addExercise(ex4);
        exerciseRepository.save(ex4);

        Map<String, String> pairs = new HashMap<>();
        pairs.put("der Hund", "the dog (m)");
        pairs.put("die Katze", "the cat (f)");
        pairs.put("das Haus", "the house (n)");
        MatchExercise ex5 = new MatchExercise();
        ex5.setQuestion("Match the nouns with their articles");
        ex5.setCorrectAnswer("der Hund:the dog (m);die Katze:the cat (f);das Haus:the house (n)");
        ex5.setPairs(pairs);
        ex5.setExplanation("der=masculine, die=feminine, das=neuter");
        lesson.addExercise(ex5);
        exerciseRepository.save(ex5);

        FillBlankExercise ex6 = new FillBlankExercise();
        ex6.setQuestion("Complete: The apple (masculine)");
        ex6.setCorrectAnswer("der");
        ex6.setSentenceTemplate("___ Apfel");
        ex6.setExplanation("'Apfel' is masculine, so it uses 'der'");
        lesson.addExercise(ex6);
        exerciseRepository.save(ex6);
    }

    private void createLesson5Verbs(Unit unit) {
        Lesson lesson = new Lesson("Simple Verbs", 1, "Learn sein (to be) and haben (to have)");
        unit.addLesson(lesson);
        lessonRepository.save(lesson);

        FillBlankExercise ex1 = new FillBlankExercise();
        ex1.setQuestion("Complete: I am tired");
        ex1.setCorrectAnswer("bin");
        ex1.setSentenceTemplate("Ich ___ müde.");
        ex1.setExplanation("'Ich bin' = 'I am'. 'Sein' conjugates to 'bin' for 'ich'");
        lesson.addExercise(ex1);
        exerciseRepository.save(ex1);

        MultipleChoiceExercise ex2 = new MultipleChoiceExercise();
        ex2.setQuestion("How do you say 'You are' (informal) in German?");
        ex2.setCorrectAnswer("Du bist");
        ex2.setOptionsFromArray(new String[]{"Ich bin", "Du bist", "Er ist", "Wir sind"});
        ex2.setCorrectOptionIndex(1);
        ex2.setExplanation("'Du bist' = 'You are' (informal singular)");
        lesson.addExercise(ex2);
        exerciseRepository.save(ex2);

        FillBlankExercise ex3 = new FillBlankExercise();
        ex3.setQuestion("Complete: He has a car");
        ex3.setCorrectAnswer("hat");
        ex3.setSentenceTemplate("Er ___ ein Auto.");
        ex3.setExplanation("'Er hat' = 'He has'. 'Haben' conjugates to 'hat' for 'er/sie/es'");
        lesson.addExercise(ex3);
        exerciseRepository.save(ex3);

        OrderExercise ex4 = new OrderExercise();
        ex4.setQuestion("Arrange: We are happy");
        ex4.setCorrectAnswer("Wir sind glücklich");
        ex4.setCorrectOrder("Wir sind glücklich");
        ex4.setShuffledWordsFromList(java.util.List.of("glücklich", "Wir", "sind"));
        ex4.setExplanation("'Wir sind' = 'We are'");
        lesson.addExercise(ex4);
        exerciseRepository.save(ex4);

        Map<String, String> pairs = new HashMap<>();
        pairs.put("ich bin", "I am");
        pairs.put("du hast", "you have");
        pairs.put("er ist", "he is");
        MatchExercise ex5 = new MatchExercise();
        ex5.setQuestion("Match the verb forms with their meanings");
        ex5.setCorrectAnswer("ich bin:I am;du hast:you have;er ist:he is");
        ex5.setPairs(pairs);
        ex5.setExplanation("'sein' (to be) and 'haben' (to have) are the most important German verbs");
        lesson.addExercise(ex5);
        exerciseRepository.save(ex5);

        FillBlankExercise ex6 = new FillBlankExercise();
        ex6.setQuestion("Complete: They have time");
        ex6.setCorrectAnswer("haben");
        ex6.setSentenceTemplate("Sie ___ Zeit.");
        ex6.setExplanation("'Sie haben' = 'They have' (or formal 'You have')");
        lesson.addExercise(ex6);
        exerciseRepository.save(ex6);
    }
}
