package com.campus.lostfound.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for submitting a claim on a found item report.
 */
public class ClaimSubmissionRequest {

    @NotNull(message = "Item report ID is required")
    private Long itemReportId;

    // Optional: Ignored and derived server-side from the authenticated principal
    private Long claimantId;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    public ClaimSubmissionRequest() {
    }

    public ClaimSubmissionRequest(Long itemReportId, String description) {
        this.itemReportId = itemReportId;
        this.description = description;
    }

    public ClaimSubmissionRequest(Long itemReportId, Long claimantId, String description) {
        this.itemReportId = itemReportId;
        this.claimantId = claimantId;
        this.description = description;
    }

    public Long getItemReportId() {
        return itemReportId;
    }

    public void setItemReportId(Long itemReportId) {
        this.itemReportId = itemReportId;
    }

    public Long getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(Long claimantId) {
        this.claimantId = claimantId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

