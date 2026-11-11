package com.smartcampushelpdesk.helpdesk_api.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseConnectionTest implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseConnectionTest(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        String sql = """
                SELECT current_database(), current_schema(), version()
                """;

        jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
            System.out.println("========== DATABASE TEST ==========");
            System.out.println("Database: " + rs.getString(1));
            System.out.println("Schema: " + rs.getString(2));
            System.out.println("Version: " + rs.getString(3));
            System.out.println("Connection successful!");
            System.out.println("===================================");
            return null;
        });
    }
}