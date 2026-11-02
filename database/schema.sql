-- =============================================================================
-- Database Schema for Campus Lost & Found Portal
-- Database: campus_lost_found
-- Compatible with MySQL 8.0+ / InnoDB / utf8mb4
-- =============================================================================

-- Ensure we are using the designated database
USE campus_lost_found;

-- -----------------------------------------------------------------------------
-- Table 1: users
-- Corresponds to com.campus.lostfound.entity.User
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    college_uid VARCHAR(50) NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    password_hash VARCHAR(255) NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'STUDENT',
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_users_college_uid UNIQUE (college_uid),
    CONSTRAINT uk_users_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Table 2: item_reports
-- Corresponds to com.campus.lostfound.entity.ItemReport
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS item_reports (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(150) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    private_description VARCHAR(1000) NULL,
    category VARCHAR(50) NOT NULL,
    location VARCHAR(100) NOT NULL,
    report_type VARCHAR(20) NOT NULL,
    report_date DATE NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_item_reports_user FOREIGN KEY (user_id)
        REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    INDEX idx_item_reports_user_id (user_id),
    INDEX idx_item_reports_category (category),
    INDEX idx_item_reports_location (location),
    INDEX idx_item_reports_status (status),
    INDEX idx_item_reports_report_type (report_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Table 3: claims
-- Corresponds to com.campus.lostfound.entity.Claim
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS claims (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    item_id BIGINT NOT NULL,
    claimant_id BIGINT NOT NULL,
    description VARCHAR(1000) NOT NULL,
    identifying_features VARCHAR(1000) NULL,
    claim_date DATE NOT NULL,
    claim_status VARCHAR(30) NOT NULL,
    CONSTRAINT fk_claims_item_report FOREIGN KEY (item_id)
        REFERENCES item_reports (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_claims_claimant FOREIGN KEY (claimant_id)
        REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    INDEX idx_claims_item_id (item_id),
    INDEX idx_claims_claimant_id (claimant_id),
    INDEX idx_claims_claim_status (claim_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- Table 4: evidence_files
-- Corresponds to com.campus.lostfound.entity.EvidenceFile
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS evidence_files (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    storage_key VARCHAR(255) NOT NULL,
    evidence_type VARCHAR(50) NOT NULL,
    item_report_id BIGINT NULL,
    claim_id BIGINT NULL,
    uploaded_by_user_id BIGINT NOT NULL,
    original_filename VARCHAR(255) NULL,
    content_type VARCHAR(100) NOT NULL,
    file_size BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_evidence_storage_key UNIQUE (storage_key),
    CONSTRAINT fk_evidence_item_report FOREIGN KEY (item_report_id)
        REFERENCES item_reports (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_evidence_claim FOREIGN KEY (claim_id)
        REFERENCES claims (id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT fk_evidence_uploaded_by FOREIGN KEY (uploaded_by_user_id)
        REFERENCES users (id)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    INDEX idx_evidence_item_report_id (item_report_id),
    INDEX idx_evidence_claim_id (claim_id),
    INDEX idx_evidence_uploaded_by (uploaded_by_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
