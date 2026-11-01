-- =============================================================================
-- Migration v2: Secure Authentication Schema Extension
-- Adds college_uid, password_hash, and role to the users table.
-- Compatible with MySQL 8.0+ / InnoDB / utf8mb4.
-- Preserves existing user IDs, item reports, and claims history without data loss.
-- =============================================================================

USE campus_lost_found;

-- Add authentication columns:
-- college_uid: Unique campus identifier (e.g. 24BCS10586). Nullable for historical legacy rows.
-- password_hash: BCrypt password hash. Nullable for historical legacy rows until account-linking.
-- role: Role-based authorization ('STUDENT' or 'ADMIN'). Defaults to 'STUDENT'.
-- enabled: Whether the account is active. Defaults to TRUE.
ALTER TABLE users
    ADD COLUMN college_uid VARCHAR(50) NULL AFTER id,
    ADD COLUMN password_hash VARCHAR(255) NULL AFTER email,
    ADD COLUMN role VARCHAR(20) NOT NULL DEFAULT 'STUDENT' AFTER password_hash,
    ADD COLUMN enabled BOOLEAN NOT NULL DEFAULT TRUE AFTER role;

-- Enforce unique college_uid across all accounts
ALTER TABLE users
    ADD CONSTRAINT uk_users_college_uid UNIQUE (college_uid);

-- Index on role for fast authorization lookups
CREATE INDEX idx_users_role ON users (role);

