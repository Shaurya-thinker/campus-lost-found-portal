-- =============================================================================
-- Migration V3: Secure Claim Verification and Private Evidence
-- Database: campus_lost_found
-- Non-destructive extension for Milestone 13
-- =============================================================================

USE campus_lost_found;

-- 1. Add private_description column to item_reports table for distinguishing details
ALTER TABLE item_reports
ADD COLUMN private_description VARCHAR(1000) NULL AFTER description;

-- 2. Add identifying_features column to claims table
ALTER TABLE claims
ADD COLUMN identifying_features VARCHAR(1000) NULL AFTER description;

-- 3. Widen claim_status column on claims table to accommodate MORE_INFO_REQUESTED (19 chars)
ALTER TABLE claims
MODIFY COLUMN claim_status VARCHAR(30) NOT NULL;

-- 4. Create evidence_files table for private photo evidence metadata
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

