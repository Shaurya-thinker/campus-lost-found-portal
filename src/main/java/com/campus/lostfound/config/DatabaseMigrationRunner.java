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
 * and Milestone 13 evidence columns/tables are present without destroying or resetting existing data.
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
            log.info("Checking database schema for authentication and evidence extensions...");

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

            // Milestone 13: Check if column private_description exists in item_reports table
            Integer privateDescCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'item_reports' AND COLUMN_NAME = 'private_description'",
                    Integer.class
            );

            if (privateDescCount != null && privateDescCount == 0) {
                log.info("Applying migration: adding private_description to item_reports table...");
                jdbcTemplate.execute("ALTER TABLE item_reports ADD COLUMN private_description VARCHAR(1000) NULL AFTER description");
                log.info("Successfully added private_description column.");
            }

            // Milestone 13: Check if column identifying_features exists in claims table
            Integer identifyingFeaturesCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.COLUMNS " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'claims' AND COLUMN_NAME = 'identifying_features'",
                    Integer.class
            );

            if (identifyingFeaturesCount != null && identifyingFeaturesCount == 0) {
                log.info("Applying migration: adding identifying_features to claims table...");
                jdbcTemplate.execute("ALTER TABLE claims ADD COLUMN identifying_features VARCHAR(1000) NULL AFTER description");
                log.info("Successfully added identifying_features column.");
            }

            // Milestone 13: Ensure claim_status is VARCHAR(30) to support MORE_INFO_REQUESTED
            try {
                jdbcTemplate.execute("ALTER TABLE claims MODIFY COLUMN claim_status VARCHAR(30) NOT NULL");
            } catch (Exception ex) {
                log.debug("Notice on claim_status modify: {}", ex.getMessage());
            }

            // Milestone 13: Check if evidence_files table exists
            Integer evidenceFilesTableCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.TABLES " +
                            "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = 'evidence_files'",
                    Integer.class
            );

            if (evidenceFilesTableCount != null && evidenceFilesTableCount == 0) {
                log.info("Applying migration: creating evidence_files table...");
                jdbcTemplate.execute("CREATE TABLE evidence_files (" +
                        "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                        "storage_key VARCHAR(255) NOT NULL, " +
                        "evidence_type VARCHAR(50) NOT NULL, " +
                        "item_report_id BIGINT NULL, " +
                        "claim_id BIGINT NULL, " +
                        "uploaded_by_user_id BIGINT NOT NULL, " +
                        "original_filename VARCHAR(255) NULL, " +
                        "content_type VARCHAR(100) NOT NULL, " +
                        "file_size BIGINT NOT NULL, " +
                        "created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                        "CONSTRAINT uk_evidence_storage_key UNIQUE (storage_key), " +
                        "CONSTRAINT fk_evidence_item_report FOREIGN KEY (item_report_id) REFERENCES item_reports (id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                        "CONSTRAINT fk_evidence_claim FOREIGN KEY (claim_id) REFERENCES claims (id) ON DELETE CASCADE ON UPDATE CASCADE, " +
                        "CONSTRAINT fk_evidence_uploaded_by FOREIGN KEY (uploaded_by_user_id) REFERENCES users (id) ON DELETE RESTRICT ON UPDATE CASCADE, " +
                        "INDEX idx_evidence_item_report_id (item_report_id), " +
                        "INDEX idx_evidence_claim_id (claim_id), " +
                        "INDEX idx_evidence_uploaded_by (uploaded_by_user_id)" +
                        ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci");
                log.info("Successfully created evidence_files table.");
            }

            log.info("Database schema verification for authentication and evidence completed successfully.");
        } catch (Exception ex) {
            log.warn("Database migration check completed with notice (safe if table/driver does not use information_schema): {}", ex.getMessage());
        }
    }
}
