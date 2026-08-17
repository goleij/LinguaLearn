package com.germanlearning.config;

import com.germanlearning.model.ActivityPhase;
import com.germanlearning.model.ActivityType;
import com.germanlearning.model.Difficulty;
import com.germanlearning.model.Skill;
import com.germanlearning.service.activity.ActivityPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts a database written by the old exercise model into lesson
 * activities, once.
 *
 * Deliberately uses plain SQL: the Exercise entities are gone, and a migration
 * should read what is actually in the database rather than what the current
 * mapping expects. Ordering, XP rewards and explanations are preserved, the
 * last two exercises of every lesson become the checkpoint, and each progress
 * row's XP bookkeeping is rewritten to the new activity ids so no learner is
 * paid twice for content they already answered.
 */
@Component
@Order(10) // before DataInitializer
public class LegacyExerciseMigrator implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(LegacyExerciseMigrator.class);

    /** How many trailing exercises of a migrated lesson become the checkpoint. */
    private static final int CHECKPOINT_TAIL = 2;

    private final JdbcTemplate jdbc;

    public LegacyExerciseMigrator(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (!tableExists("exercises") || countRows("lesson_activities") > 0 || countRows("exercises") == 0) {
            return;
        }

        List<Map<String, Object>> exercises = jdbc.queryForList(
                "select * from exercises order by lesson_id asc, id asc");

        Map<Long, List<Map<String, Object>>> byLesson = new LinkedHashMap<>();
        for (Map<String, Object> exercise : exercises) {
            byLesson.computeIfAbsent(asLong(exercise.get("lesson_id")), key -> new ArrayList<>()).add(exercise);
        }

        Map<Long, Long> exerciseToActivity = new LinkedHashMap<>();
        for (Map.Entry<Long, List<Map<String, Object>>> entry : byLesson.entrySet()) {
            migrateLesson(entry.getKey(), entry.getValue(), exerciseToActivity);
        }

        remapAwardedXpIds(exerciseToActivity);

        log.info("Migrated {} legacy exercises into lesson activities", exerciseToActivity.size());
    }

    private void migrateLesson(Long lessonId, List<Map<String, Object>> exercises,
            Map<Long, Long> exerciseToActivity) {
        int checkpointFrom = Math.max(1, exercises.size() - CHECKPOINT_TAIL);

        for (int position = 0; position < exercises.size(); position++) {
            Map<String, Object> exercise = exercises.get(position);
            ActivityType type = mapType(asString(exercise.get("exercise_type_enum")));
            ActivityPhase phase = position >= checkpointFrom ? ActivityPhase.CHECKPOINT : ActivityPhase.PRACTICE;

            jdbc.update("""
                    insert into lesson_activities
                      (lesson_id, type, phase, position, instruction, prompt, context, hint,
                       explanation, skill, difficulty, topic, xp_reward, correct_answer, payload)
                    values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)
                    """,
                    lessonId,
                    type.name(),
                    phase.name(),
                    position,
                    instructionFor(type),
                    asString(exercise.get("question")),
                    null,
                    null,
                    asString(exercise.get("explanation")),
                    skillFor(type).name(),
                    Difficulty.MEDIUM.name(),
                    null,
                    exercise.get("xp_reward") == null ? 10 : asLong(exercise.get("xp_reward")).intValue(),
                    correctAnswerFor(type, exercise),
                    payloadFor(type, exercise));

            Long activityId = jdbc.queryForObject("select last_insert_rowid()", Long.class);
            exerciseToActivity.put(asLong(exercise.get("id")), activityId);
        }
    }

    /**
     * Rewrites progress.xp_awarded_activity_ids from the ids stored by the old
     * model, so activities that already paid out stay paid out.
     */
    private void remapAwardedXpIds(Map<Long, Long> exerciseToActivity) {
        if (!columnExists("progress", "xp_awarded_exercise_ids")) {
            return;
        }

        List<Map<String, Object>> rows = jdbc.queryForList(
                "select id, xp_awarded_exercise_ids from progress where xp_awarded_exercise_ids is not null");

        for (Map<String, Object> row : rows) {
            String legacyIds = asString(row.get("xp_awarded_exercise_ids"));
            if (legacyIds == null || legacyIds.isBlank()) {
                continue;
            }

            String migrated = Arrays.stream(legacyIds.split(","))
                    .map(String::trim)
                    .filter(id -> !id.isEmpty())
                    .map(id -> exerciseToActivity.get(Long.valueOf(id)))
                    .filter(java.util.Objects::nonNull)
                    .map(String::valueOf)
                    .reduce((a, b) -> a + "," + b)
                    .orElse("");

            jdbc.update("update progress set xp_awarded_activity_ids = ? where id = ?",
                    migrated, asLong(row.get("id")));
        }
    }

    // ------------------------------------------------------------- mapping

    private ActivityType mapType(String legacyType) {
        if (legacyType == null) {
            return ActivityType.MULTIPLE_CHOICE;
        }
        return switch (legacyType) {
            case "FILL_BLANK" -> ActivityType.FILL_BLANK;
            case "MATCH_PAIRS" -> ActivityType.MATCH_PAIRS;
            case "SENTENCE_ORDER" -> ActivityType.SENTENCE_BUILDER;
            default -> ActivityType.MULTIPLE_CHOICE;
        };
    }

    private Skill skillFor(ActivityType type) {
        return switch (type) {
            case SENTENCE_BUILDER -> Skill.WORD_ORDER;
            case FILL_BLANK -> Skill.GRAMMAR;
            default -> Skill.VOCABULARY;
        };
    }

    private String instructionFor(ActivityType type) {
        return switch (type) {
            case FILL_BLANK -> "Fill in the blank";
            case MATCH_PAIRS -> "Match the pairs";
            case SENTENCE_BUILDER -> "Arrange the words in correct order";
            default -> "Choose the correct answer";
        };
    }

    private String correctAnswerFor(ActivityType type, Map<String, Object> exercise) {
        if (type == ActivityType.SENTENCE_BUILDER) {
            String correctOrder = asString(exercise.get("correct_order"));
            if (correctOrder != null && !correctOrder.isBlank()) {
                return correctOrder;
            }
        }
        return asString(exercise.get("correct_answer"));
    }

    private String payloadFor(ActivityType type, Map<String, Object> exercise) {
        return switch (type) {
            case MULTIPLE_CHOICE -> ActivityPayload.write(
                    "options", splitOn(asString(exercise.get("options")), "\\|"),
                    "correctIndex", exercise.get("correct_option_index") == null
                            ? 0
                            : asLong(exercise.get("correct_option_index")).intValue());
            case FILL_BLANK -> ActivityPayload.write(
                    "sentence", asString(exercise.get("sentence_template")));
            case SENTENCE_BUILDER -> ActivityPayload.write(
                    "words", splitOn(asString(exercise.get("shuffled_words")), "\\|"));
            case MATCH_PAIRS -> ActivityPayload.write(
                    "pairs", parsePairs(asString(exercise.get("pairs_data"))));
            default -> null;
        };
    }

    private List<String> splitOn(String value, String separator) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        return Arrays.stream(value.split(separator)).map(String::trim).toList();
    }

    private Map<String, String> parsePairs(String pairsData) {
        Map<String, String> pairs = new LinkedHashMap<>();
        if (pairsData == null || pairsData.isBlank()) {
            return pairs;
        }
        for (String pair : pairsData.split(";")) {
            String[] parts = pair.split(":", 2);
            if (parts.length == 2) {
                pairs.put(parts[0].trim(), parts[1].trim());
            }
        }
        return pairs;
    }

    // ------------------------------------------------------------- plumbing

    private boolean tableExists(String table) {
        Integer count = jdbc.queryForObject(
                "select count(*) from sqlite_master where type = 'table' and name = ?", Integer.class, table);
        return count != null && count > 0;
    }

    private boolean columnExists(String table, String column) {
        List<Map<String, Object>> columns = jdbc.queryForList("pragma table_info(" + table + ")");
        return columns.stream().anyMatch(row -> column.equalsIgnoreCase(asString(row.get("name"))));
    }

    private int countRows(String table) {
        if (!tableExists(table)) {
            return 0;
        }
        Integer count = jdbc.queryForObject("select count(*) from " + table, Integer.class);
        return count == null ? 0 : count;
    }

    private Long asLong(Object value) {
        return value == null ? null : ((Number) value).longValue();
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
