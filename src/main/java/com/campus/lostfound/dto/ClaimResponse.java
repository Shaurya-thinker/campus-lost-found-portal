package com.campus.lostfound.dto;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.enums.ClaimStatus;

import java.time.LocalDate;

/**
 * Response payload representing claim details without exposing entity
 * internals.
 * Contains claimant's own submitted details; strictly excludes reporter private
 * details.
 */
public class ClaimResponse {

    private Long id;
    private Long itemReportId;
    private String itemTitle;
    private Long claimantId;
    private String claimantName;
    private String description;
    private String identifyingFeatures;
    private Long claimantEvidenceId;
    private LocalDate claimDate;
    private ClaimStatus claimStatus;

    public ClaimResponse() {
    }

    public ClaimResponse(Long id, Long itemReportId, String itemTitle, Long claimantId,
            String claimantName, String description, LocalDate claimDate,
            ClaimStatus claimStatus) {
        this.id = id;
        this.itemReportId = itemReportId;
        this.itemTitle = itemTitle;
        this.claimantId = claimantId;
        this.claimantName = claimantName;
        this.description = description;
        this.claimDate = claimDate;
        this.claimStatus = claimStatus;
    }

    public ClaimResponse(Long id, Long itemReportId, String itemTitle, Long claimantId,
            String claimantName, String description, String identifyingFeatures,
            Long claimantEvidenceId, LocalDate claimDate, ClaimStatus claimStatus) {
        this.id = id;
        this.itemReportId = itemReportId;
        this.itemTitle = itemTitle;
        this.claimantId = claimantId;
        this.claimantName = claimantName;
        this.description = description;
        this.identifyingFeatures = identifyingFeatures;
        this.claimantEvidenceId = claimantEvidenceId;
        this.claimDate = claimDate;
        this.claimStatus = claimStatus;
    }

    public static ClaimResponse fromEntity(Claim claim) {
        return fromEntity(claim, null);
    }

    public static ClaimResponse fromEntity(Claim claim, Long claimantEvidenceId) {
        if (claim == null) {
            return null;
        }
        return new ClaimResponse(
                claim.getId(),
                claim.getItemReport() != null ? claim.getItemReport().getId() : null,
                claim.getItemReport() != null ? claim.getItemReport().getTitle() : null,
                claim.getClaimant() != null ? claim.getClaimant().getId() : null,
                claim.getClaimant() != null ? claim.getClaimant().getName() : null,
                claim.getDescription(),
                claim.getIdentifyingFeatures(),
                claimantEvidenceId,
                claim.getClaimDate(),
                claim.getClaimStatus());
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getItemReportId() {
        return itemReportId;
    }

    public void setItemReportId(Long itemReportId) {
        this.itemReportId = itemReportId;
    }

    public String getItemTitle() {
        return itemTitle;
    }

    public void setItemTitle(String itemTitle) {
        this.itemTitle = itemTitle;
    }

    public Long getClaimantId() {
        return claimantId;
    }

    public void setClaimantId(Long claimantId) {
        this.claimantId = claimantId;
    }

    public String getClaimantName() {
        return claimantName;
    }

    public void setClaimantName(String claimantName) {
        this.claimantName = claimantName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getIdentifyingFeatures() {
        return identifyingFeatures;
    }

    public void setIdentifyingFeatures(String identifyingFeatures) {
        this.identifyingFeatures = identifyingFeatures;
    }

    public Long getClaimantEvidenceId() {
        return claimantEvidenceId;
    }

    public void setClaimantEvidenceId(Long claimantEvidenceId) {
        this.claimantEvidenceId = claimantEvidenceId;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
    }
}
