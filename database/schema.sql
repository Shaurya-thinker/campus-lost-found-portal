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
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
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
    claim_date DATE NOT NULL,
    claim_status VARCHAR(20) NOT NULL,
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

