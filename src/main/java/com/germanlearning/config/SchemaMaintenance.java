package com.germanlearning.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Removes the CHECK constraints Hibernate generates for enum columns.
 *
 * Hibernate writes the enum values into the table definition, e.g.
 * {@code check (type in ('LEARN_CARD', ...))}. That list is frozen at the
 * moment the table is created: {@code ddl-auto=update} adds columns but never
 * rewrites a constraint, and SQLite cannot alter one at all. So the first time
 * a new activity type, phase or level is added, every existing database
 * rejects it — which is exactly what happened when VOCABULARY, GRAMMAR_TIP,
 * REAL_EXAMPLE and the new phases arrived.
 *
 * The values are already guaranteed by the enums on the Java side, so the
 * constraint buys nothing and costs a broken start-up on every future
 * addition. This runs before any seeding and rebuilds affected tables without
 * those constraints, keeping their rows, columns, defaults, keys and indexes.
 */
@Component
@Order(0) // before LegacyExerciseMigrator and DataInitializer
public class SchemaMaintenance implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMaintenance.class);

    /** "check (...)" including one level of nested parentheses, e.g. an IN list. */
    private static final Pattern CHECK_CONSTRAINT =
            Pattern.compile("\\s*check\\s*\\((?:[^()]|\\([^()]*\\))*\\)", Pattern.CASE_INSENSITIVE);

    private final JdbcTemplate jdbc;

    public SchemaMaintenance(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    @Transactional
    public void run(String... args) {
        List<Map<String, Object>> tables = jdbc.queryForList(
                "select name, sql from sqlite_master where type = 'table' and sql like '%check (%'");

        for (Map<String, Object> table : tables) {
            String name = String.valueOf(table.get("name"));
            String sql = String.valueOf(table.get("sql"));
            rebuildWithoutChecks(name, sql);
        }
    }

    private void rebuildWithoutChecks(String table, String originalSql) {
        String cleanedSql = CHECK_CONSTRAINT.matcher(originalSql).replaceAll("");
        if (cleanedSql.equals(originalSql)) {
            return;
        }

        String tempTable = table + "_ll_tmp";
        String createTemp = replaceFirstTableName(cleanedSql, table, tempTable);
        if (createTemp == null) {
            log.warn("Could not rewrite the definition of '{}'; leaving it untouched", table);
            return;
        }

        // Indexes live in their own rows and disappear with the old table
        List<String> indexes = jdbc.queryForList(
                        "select sql from sqlite_master where type = 'index' and tbl_name = ? and sql is not null",
                        String.class, table)
                .stream()
                .filter(sql -> sql != null && !sql.isBlank())
                .toList();

        jdbc.execute(createTemp);
        jdbc.execute("insert into " + tempTable + " select * from " + table);
        jdbc.execute("drop table " + table);
        jdbc.execute("alter table " + tempTable + " rename to " + table);
        indexes.forEach(jdbc::execute);

        log.info("Rebuilt '{}' without enum check constraints so new enum values are accepted", table);
    }

    /** Swaps the table name in "create table <name> (" for the temporary one. */
    private String replaceFirstTableName(String createSql, String table, String tempTable) {
        Pattern pattern = Pattern.compile(
                "(create\\s+table\\s+)([\"'`\\[]?)" + Pattern.quote(table) + "([\"'`\\]]?)",
                Pattern.CASE_INSENSITIVE);
        var matcher = pattern.matcher(createSql);
        return matcher.find() ? matcher.replaceFirst("$1" + tempTable) : null;
    }
}
