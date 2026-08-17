package com.germanlearning.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The enum CHECK constraints Hibernate freezes into a table are what broke
 * start-up when new activity types were added, so the repair is tested against
 * a real SQLite file rather than mocked away.
 */
class SchemaMaintenanceTest {

    private JdbcTemplate jdbcFor(Path directory, String fileName) {
        DriverManagerDataSource dataSource = new DriverManagerDataSource(
                "jdbc:sqlite:" + directory.resolve(fileName));
        dataSource.setDriverClassName("org.sqlite.JDBC");
        return new JdbcTemplate(dataSource);
    }

    @Test
    void dropsTheStaleEnumConstraintAndKeepsTheRows(@TempDir Path directory) {
        JdbcTemplate jdbc = jdbcFor(directory, "legacy.db");

        jdbc.execute("""
                create table lesson_activities (
                    id integer,
                    phase varchar(255) not null check (phase in ('LEARN','PRACTICE','CHECKPOINT')),
                    type varchar(255) not null check (type in ('LEARN_CARD','MULTIPLE_CHOICE')),
                    prompt varchar(1000),
                    primary key (id)
                )
                """);
        jdbc.execute("create index idx_activity_phase on lesson_activities (phase)");
        jdbc.update("insert into lesson_activities values (1, 'PRACTICE', 'MULTIPLE_CHOICE', 'old row')");

        // Before the repair the new values are rejected
        assertThrows(Exception.class, () -> jdbc.update(
                "insert into lesson_activities values (2, 'GUIDED_PRACTICE', 'VOCABULARY', 'new row')"));

        new SchemaMaintenance(jdbc).run();

        // The row, its columns and the index survive
        assertEquals(1, jdbc.queryForObject("select count(*) from lesson_activities", Integer.class));
        assertEquals("old row",
                jdbc.queryForObject("select prompt from lesson_activities where id = 1", String.class));
        assertEquals(1, jdbc.queryForObject(
                "select count(*) from sqlite_master where type = 'index' and name = 'idx_activity_phase'",
                Integer.class));

        // And the values that used to be rejected now insert
        assertDoesNotThrow(() -> jdbc.update(
                "insert into lesson_activities values (2, 'GUIDED_PRACTICE', 'VOCABULARY', 'new row')"));

        String schema = jdbc.queryForObject(
                "select sql from sqlite_master where type = 'table' and name = 'lesson_activities'",
                String.class);
        assertFalse(schema.toLowerCase().contains("check ("));
        assertTrue(schema.contains("not null"), "other column rules stay in place");
    }

    @Test
    void leavesTablesWithoutCheckConstraintsAlone(@TempDir Path directory) {
        JdbcTemplate jdbc = jdbcFor(directory, "clean.db");

        jdbc.execute("create table users (id integer, username varchar(50) not null, primary key (id))");
        jdbc.update("insert into users values (1, 'anna')");

        String before = jdbc.queryForObject(
                "select sql from sqlite_master where type = 'table' and name = 'users'", String.class);

        new SchemaMaintenance(jdbc).run();

        String after = jdbc.queryForObject(
                "select sql from sqlite_master where type = 'table' and name = 'users'", String.class);

        assertEquals(before, after);
        assertEquals(1, jdbc.queryForObject("select count(*) from users", Integer.class));
    }
}
