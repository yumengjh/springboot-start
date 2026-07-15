package com.yumg.starter.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@ActiveProfiles("local")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class SqliteMigrationTest {

    private static final Set<String> CORE_TABLES = Set.of("users", "roles", "permissions",
            "user_roles", "role_permissions", "refresh_sessions", "system_settings",
            "ip_access_rules", "audit_events", "announcements", "navigation_menus");

    @TempDir
    static Path temporaryDirectory;

    @Autowired
    private JdbcTemplate jdbc;

    @DynamicPropertySource
    static void sqliteProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url",
                () -> "jdbc:sqlite:" + temporaryDirectory.resolve("migration.db"));
        registry.add("spring.datasource.driver-class-name", () -> "org.sqlite.JDBC");
    }

    @Test
    void liquibaseCreatesCoreTables() {
        List<String> tables = jdbc.queryForList(
                "select name from sqlite_master where type = 'table'", String.class);

        assertThat(tables).containsAll(CORE_TABLES);
        assertThat(jdbc.queryForObject("select count(*) from databasechangelog", Integer.class))
                .isGreaterThanOrEqualTo(3);

        assertThat(indexColumns("users", true)).contains(List.of("username"));
        assertThat(indexColumns("refresh_sessions", true)).contains(List.of("token_hash"));
        assertThat(indexColumns("user_roles", true)).contains(List.of("user_id", "role_id"));
        assertThat(indexColumns("role_permissions", true))
                .contains(List.of("role_id", "permission_id"));
        assertThat(indexColumns("navigation_menus", true))
                .contains(List.of("code"), List.of("route_path"));
        assertThat(indexNames("users")).contains("idx_users_username");
        assertThat(indexNames("permissions")).contains("idx_permissions_code");
        assertThat(indexNames("refresh_sessions")).contains("idx_refresh_sessions_token_hash");
        assertThat(indexNames("audit_events")).contains("idx_audit_events_occurred_at");
        assertThat(indexNames("ip_access_rules")).contains("idx_ip_access_rules_lookup");
        assertThat(indexNames("announcements")).contains("idx_announcements_publication");
        assertThat(indexNames("navigation_menus")).contains("idx_navigation_menus_parent_sort");

        assertThat(foreignKeyTargets("refresh_sessions")).contains("users");
        assertThat(foreignKeyTargets("announcements")).contains("users");
        assertThat(foreignKeyTargets("role_permissions")).contains("roles", "permissions");
        assertThat(columnType("users", "id")).isEqualToIgnoringCase("varchar(36)");
        assertThat(columnType("users", "status")).isEqualToIgnoringCase("varchar(32)");
        assertThat(columnType("audit_events", "version")).isEqualToIgnoringCase("bigint");
    }

    @Test
    void rejectsRefreshSessionForMissingUser() {
        assertThat(jdbc.queryForObject("pragma foreign_keys", Integer.class)).isEqualTo(1);

        assertThatThrownBy(() -> jdbc.update("""
                insert into refresh_sessions
                    (id, user_id, family_id, token_hash, issued_at, expires_at, version)
                values (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
                """, "00000000-0000-0000-0000-000000000001",
                "00000000-0000-0000-0000-000000000099",
                "00000000-0000-0000-0000-000000000002",
                "0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef"))
                .isInstanceOf(DataAccessException.class);
    }

    private List<String> indexNames(String table) {
        return jdbc.queryForList("select name from pragma_index_list(?)", String.class, table);
    }

    private List<List<String>> indexColumns(String table, boolean unique) {
        List<String> indexes = jdbc.queryForList(
                "select name from pragma_index_list(?) where \"unique\" = ?", String.class,
                table, unique ? 1 : 0);
        return indexes.stream().map(index -> jdbc.queryForList(
                "select name from pragma_index_info(?) order by seqno", String.class, index)).toList();
    }

    private List<String> foreignKeyTargets(String table) {
        return jdbc.queryForList("select \"table\" from pragma_foreign_key_list(?)", String.class,
                table);
    }

    private String columnType(String table, String column) {
        return jdbc.queryForObject("select type from pragma_table_info(?) where name = ?",
                String.class, table, column);
    }
}
