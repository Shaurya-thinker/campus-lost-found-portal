package com.campus.lostfound.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Idempotent, safe startup database migration runner.
 * Automatically ensures authentication columns (college_uid, password_hash, role)
 * are present on the 'users' table without destroying or resetting existing data.
 */
@Component
@Order(1)
public class DatabaseMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseMigrationRunner.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            log.info("Checking database schema for authentication extension...");

            // Check if column college_uid exists in users table
            Integer collegeUidCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'college_uid'",
                    Integer.class
            );

            if (collegeUidCount != null && collegeUidCount == 0) {
                log.info("Applying migration: adding college_uid to users table...");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN college_uid VARCHAR(50) NULL AFTER id");
                jdbcTemplate.execute("ALTER TABLE users ADD CONSTRAINT uk_users_college_uid UNIQUE (college_uid)");
                log.info("Successfully added college_uid column and unique constraint.");
            }

            // Check if column password_hash exists in users table
            Integer passwordHashCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'password_hash'",
                    Integer.class
            );

            if (passwordHashCount != null && passwordHashCount == 0) {
                log.info("Applying migration: adding password_hash to users table...");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN password_hash VARCHAR(255) NULL AFTER email");
                log.info("Successfully added password_hash column.");
            }

            // Check if column role exists in users table
            Integer roleCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'role'",
                    Integer.class
            );

            if (roleCount != null && roleCount == 0) {
                log.info("Applying migration: adding role to users table...");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' AFTER password_hash");
                log.info("Successfully added role column.");
            }

            // Check if column enabled exists in users table
            Integer enabledCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'users' AND COLUMN_NAME = 'enabled'",
                    Integer.class
            );

            if (enabledCount != null && enabledCount == 0) {
                log.info("Applying migration: adding enabled to users table...");
                jdbcTemplate.execute("ALTER TABLE users ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE AFTER role");
                log.info("Successfully added enabled column.");
            }

            log.info("Database schema verification for authentication completed successfully.");
        } catch (Exception ex) {
            log.warn("Database migration check completed with notice (safe if table/driver does not use information_schema): {}", ex.getMessage());
        }
    }
}

