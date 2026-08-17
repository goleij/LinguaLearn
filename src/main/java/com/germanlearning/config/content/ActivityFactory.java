package com.germanlearning.config.content;

import com.germanlearning.model.ActivityPhase;
import com.germanlearning.model.ActivityType;
import com.germanlearning.model.CefrLevel;
import com.germanlearning.model.Difficulty;
import com.germanlearning.model.LessonActivity;
import com.germanlearning.model.Skill;
import com.germanlearning.service.activity.ActivityPayload;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Builds lesson activities so content classes read like content, not like
 * entity plumbing.
 */
@Component
public class ActivityFactory {

    // ------------------------------------------------------------- teaching

    public LessonActivity vocabulary(String title, String intro, List<Map<String, String>> words,
            CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.VOCABULARY, ActivityPhase.LEARN,
                Skill.VOCABULARY, Difficulty.EASY, level, topic);
        activity.setInstruction("New words");
        activity.setPrompt(title);
        activity.setContext(intro);
        activity.setPayload(ActivityPayload.write("vocabulary", words));
        return activity;
    }

    public LessonActivity grammarTip(String title, String explanation, List<String> bullets,
            CefrLevel level, Skill skill, String topic) {
        LessonActivity activity = base(ActivityType.GRAMMAR_TIP, ActivityPhase.LEARN,
                skill, Difficulty.MEDIUM, level, topic);
        activity.setInstruction("Grammar");
        activity.setPrompt(title);
        activity.setContext(explanation);
        activity.setPayload(ActivityPayload.write("bullets", bullets));
        return activity;
    }

    public LessonActivity learnCard(String title, String body, List<Map<String, String>> examples,
            List<String> bullets, CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.LEARN_CARD, ActivityPhase.LEARN,
                null, null, level, topic);
        activity.setInstruction("Learn");
        activity.setPrompt(title);
        activity.setContext(body);
        activity.setPayload(ActivityPayload.write(
                "examples", examples == null ? List.of() : examples,
                "bullets", bullets == null ? List.of() : bullets));
        return activity;
    }

    /** A conversation shown as context; no answer is expected. */
    public LessonActivity dialogueScene(String title, String situation, List<Map<String, String>> lines,
            CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.DIALOGUE, ActivityPhase.CONTEXT,
                Skill.READING, Difficulty.EASY, level, topic);
        activity.setInstruction("Read the conversation");
        activity.setPrompt(title);
        activity.setContext(situation);
        activity.setPayload(ActivityPayload.write("lines", lines));
        return activity;
    }

    /**
     * Curated examples, optionally enriched at display time with real sentences
     * looked up for {@code exampleQuery}.
     */
    public LessonActivity realExamples(String title, String intro, String exampleQuery,
            List<Map<String, String>> examples, CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.REAL_EXAMPLE, ActivityPhase.CONTEXT,
                Skill.READING, Difficulty.MEDIUM, level, topic);
        activity.setInstruction("Language in context");
        activity.setPrompt(title);
        activity.setContext(intro);
        activity.setPayload(ActivityPayload.write(
                "exampleQuery", exampleQuery,
                "examples", examples == null ? List.of() : examples));
        return activity;
    }

    public LessonActivity checkpointCard(String title, String body, CefrLevel level) {
        LessonActivity activity = base(ActivityType.CHECKPOINT, ActivityPhase.CHECKPOINT,
                null, null, level, null);
        activity.setInstruction("Checkpoint");
        activity.setPrompt(title);
        activity.setContext(body);
        return activity;
    }

    // ---------------------------------------------------------- interactive

    public LessonActivity choice(ActivityType type, ActivityPhase phase, String instruction,
            String prompt, String context, List<String> options, int correctIndex,
            String explanation, String hint, Skill skill, Difficulty difficulty,
            CefrLevel level, String topic) {
        LessonActivity activity = base(type, phase, skill, difficulty, level, topic);
        activity.setInstruction(instruction);
        activity.setPrompt(prompt);
        activity.setContext(context);
        activity.setExplanation(explanation);
        activity.setHint(hint);
        activity.setCorrectAnswer(options.get(correctIndex));
        activity.setPayload(ActivityPayload.write("options", options, "correctIndex", correctIndex));
        return activity;
    }

    /** A dialogue that asks the learner to choose the reply, so it is graded. */
    public LessonActivity dialogueChoice(ActivityPhase phase, List<Map<String, String>> lines,
            String prompt, List<String> options, int correctIndex, String explanation, String hint,
            Difficulty difficulty, CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.DIALOGUE, phase, Skill.READING, difficulty, level, topic);
        activity.setInstruction("Continue the conversation");
        activity.setPrompt(prompt);
        activity.setExplanation(explanation);
        activity.setHint(hint);
        activity.setCorrectAnswer(options.get(correctIndex));
        activity.setPayload(ActivityPayload.write(
                "lines", lines, "options", options, "correctIndex", correctIndex));
        return activity;
    }

    public LessonActivity fillBlank(ActivityPhase phase, String prompt, String sentence,
            String correctAnswer, String explanation, String hint, Skill skill,
            Difficulty difficulty, CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.FILL_BLANK, phase, skill, difficulty, level, topic);
        activity.setInstruction("Fill in the blank");
        activity.setPrompt(prompt);
        activity.setExplanation(explanation);
        activity.setHint(hint);
        activity.setCorrectAnswer(correctAnswer);
        activity.setPayload(ActivityPayload.write("sentence", sentence));
        return activity;
    }

    public LessonActivity sentenceBuilder(ActivityPhase phase, String prompt, List<String> words,
            String correctAnswer, String explanation, String hint, Difficulty difficulty,
            CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.SENTENCE_BUILDER, phase, Skill.WORD_ORDER,
                difficulty, level, topic);
        activity.setInstruction("Arrange the words in correct order");
        activity.setPrompt(prompt);
        activity.setExplanation(explanation);
        activity.setHint(hint);
        activity.setCorrectAnswer(correctAnswer);
        activity.setPayload(ActivityPayload.write("words", words));
        return activity;
    }

    public LessonActivity matchPairs(ActivityPhase phase, String prompt, Map<String, String> pairs,
            String explanation, Skill skill, Difficulty difficulty, CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.MATCH_PAIRS, phase, skill, difficulty, level, topic);
        activity.setInstruction("Match the pairs");
        activity.setPrompt(prompt);
        activity.setExplanation(explanation);
        activity.setCorrectAnswer(pairs.entrySet().stream()
                .map(entry -> entry.getKey() + ":" + entry.getValue())
                .reduce((a, b) -> a + ";" + b)
                .orElse(""));
        activity.setPayload(ActivityPayload.write("pairs", pairs));
        return activity;
    }

    public LessonActivity translation(ActivityPhase phase, String instruction, String prompt,
            String correctAnswer, String explanation, String hint, Difficulty difficulty,
            CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.TRANSLATION, phase, Skill.WRITING, difficulty, level, topic);
        activity.setInstruction(instruction);
        activity.setPrompt(prompt);
        activity.setExplanation(explanation);
        activity.setHint(hint);
        activity.setCorrectAnswer(correctAnswer);
        return activity;
    }

    public LessonActivity listening(ActivityPhase phase, String audioText, List<String> options,
            int correctIndex, String explanation, Difficulty difficulty, CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.LISTENING, phase, Skill.LISTENING, difficulty, level, topic);
        activity.setInstruction("Listen and choose the meaning");
        activity.setPrompt("What did you hear?");
        activity.setExplanation(explanation);
        activity.setCorrectAnswer(options.get(correctIndex));
        activity.setPayload(ActivityPayload.write(
                "audioText", audioText, "audioLang", "de-DE",
                "options", options, "correctIndex", correctIndex));
        return activity;
    }

    /** The APPLY task: produce your own German, with the writing coach available. */
    public LessonActivity shortWriting(String prompt, String context, int minWords,
            List<String> mustUseWords, String explanation, CefrLevel level, String topic) {
        LessonActivity activity = base(ActivityType.SHORT_WRITING, ActivityPhase.APPLY,
                Skill.WRITING, Difficulty.HARD, level, topic);
        activity.setInstruction("Write your answer");
        activity.setPrompt(prompt);
        activity.setContext(context);
        activity.setExplanation(explanation);
        activity.setHint("Use at least " + minWords + " words");
        activity.setPayload(ActivityPayload.write("minWords", minWords, "mustUseWords", mustUseWords));
        return activity;
    }

    // --------------------------------------------------------------- shared

    private LessonActivity base(ActivityType type, ActivityPhase phase, Skill skill,
            Difficulty difficulty, CefrLevel level, String topic) {
        LessonActivity activity = new LessonActivity();
        activity.setType(type);
        activity.setPhase(phase);
        activity.setSkill(skill);
        activity.setDifficulty(difficulty);
        activity.setCefrLevel(level);
        activity.setTopic(topic);
        activity.setXpReward(type.isGraded() ? xpFor(level) : 0);
        return activity;
    }

    /** Harder levels are worth more. */
    private int xpFor(CefrLevel level) {
        if (level == null) {
            return 10;
        }
        return switch (level) {
            case A1 -> 10;
            case A2 -> 12;
            case B1 -> 15;
            case B2 -> 18;
        };
    }

    public Map<String, String> word(String german, String english) {
        return entry("de", german, "en", english);
    }

    public Map<String, String> example(String german, String english) {
        return entry("de", german, "en", english);
    }

    public Map<String, String> line(String speaker, String text) {
        return entry("speaker", speaker, "text", text);
    }

    public Map<String, String> pairs(String... keysAndValues) {
        Map<String, String> pairs = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            pairs.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return pairs;
    }

    private Map<String, String> entry(String... keysAndValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i < keysAndValues.length; i += 2) {
            map.put(keysAndValues[i], keysAndValues[i + 1]);
        }
        return map;
    }
}
