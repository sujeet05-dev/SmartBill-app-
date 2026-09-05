package com.smartbill;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
public class DatabaseConstraintMigration implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConstraintMigration.class);
    private final JdbcTemplate jdbcTemplate;

    public DatabaseConstraintMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        logger.info("Checking for global unique constraint on invoice_number...");
        try {
            // Find the auto-generated unique constraint name for invoice_number on invoices table
            String query = "SELECT tc.constraint_name " +
                           "FROM information_schema.table_constraints tc " +
                           "JOIN information_schema.constraint_column_usage AS ccu USING (constraint_schema, constraint_name) " +
                           "WHERE tc.constraint_type = 'UNIQUE' " +
                           "AND tc.table_name = 'invoices' " +
                           "AND ccu.column_name = 'invoice_number' " +
                           "LIMIT 1";
                           
            String constraintName = null;
            try {
                constraintName = jdbcTemplate.queryForObject(query, String.class);
            } catch (Exception e) {
                logger.info("No global unique constraint found for invoice_number.");
            }

            if (constraintName != null) {
                logger.info("Found global unique constraint '{}'. Dropping it to support multi-tenant sequencing...", constraintName);
                jdbcTemplate.execute("ALTER TABLE invoices DROP CONSTRAINT " + constraintName);
                logger.info("Successfully dropped global unique constraint on invoice_number.");
            }
            
            // Also explicitly try dropping the specific one the user encountered just in case it wasn't caught by the query
            try {
                jdbcTemplate.execute("ALTER TABLE invoices DROP CONSTRAINT IF EXISTS ukl1x55mfsay7co0r3m9ynvipd5");
            } catch (Exception e) {
                // Ignore
            }
            
            // Create performance indexes to speed up dashboard metrics, invoices, and inventory lookups
            logger.info("Ensuring database performance indexes exist...");
            try {
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_invoices_user_id ON invoices(user_id)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_invoices_user_date ON invoices(user_id, date DESC)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_invoices_user_is_gst ON invoices(user_id, is_gst)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_invoice_items_invoice_id ON invoice_items(invoice_id)");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_users_email_lower ON users(LOWER(email))");
                jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_products_user_id ON products(user_id)");
                logger.info("Successfully verified/created database performance indexes.");
            } catch (Exception e) {
                logger.warn("Could not create performance indexes: {}", e.getMessage());
            }

        } catch (Exception e) {
            logger.warn("Could not execute database migration: {}", e.getMessage());
        }
    }
}
