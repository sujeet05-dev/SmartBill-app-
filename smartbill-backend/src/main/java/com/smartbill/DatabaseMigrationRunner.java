package com.smartbill;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class DatabaseMigrationRunner implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) throws Exception {
        try {
            jdbcTemplate.execute("ALTER TABLE invoice_items ALTER COLUMN product_id DROP NOT NULL");
            System.out.println("SUCCESSFULLY DROPPED NOT NULL CONSTRAINT ON product_id");
        } catch (Exception e) {
            System.err.println("Error running database migration: " + e.getMessage());
        }
    }
}
