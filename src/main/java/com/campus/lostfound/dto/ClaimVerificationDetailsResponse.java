package com.campus.lostfound.dto;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.enums.ClaimStatus;
import com.campus.lostfound.entity.enums.ItemStatus;

import java.time.LocalDate;

/**
 * Administrative payload for in-depth ownership verification.
 * Presents dual-party evidence from both finder/reporter and student claimant.
 * Strictly restricted to users with ROLE_ADMIN.
 */
public class ClaimVerificationDetailsResponse {

    private Long claimId;
    private ClaimStatus claimStatus;
    private LocalDate claimDate;

    // Public item information
    private Long itemReportId;
    private String itemTitle;
    private String itemCategory;
    private String itemLocation;
    private String itemPublicDescription;
    private LocalDate reportDate;
    private ItemStatus itemStatus;

    // Finder / Reporter Private Details
    private Long reporterId;
    private String reporterName;
    private String reporterPrivateDescription;
    private Long reporterEvidenceId; // Opaque ID for GET /api/evidence/{id}

    // Claimant Ownership Evidence Details
    private Long claimantId;
    private String claimantName;
    private String claimantDescription;
    private String claimantIdentifyingFeatures;
    private Long claimantEvidenceId; // Opaque ID for GET /api/evidence/{id}

    public ClaimVerificationDetailsResponse() {
    }

    public static ClaimVerificationDetailsResponse build(
            Claim claim,
            ItemReport report,
            Long reporterEvidenceId,
            Long claimantEvidenceId) {

        ClaimVerificationDetailsResponse response = new ClaimVerificationDetailsResponse();
        if (claim != null) {
            response.setClaimId(claim.getId());
            response.setClaimStatus(claim.getClaimStatus());
            response.setClaimDate(claim.getClaimDate());
            response.setClaimantDescription(claim.getDescription());
            response.setClaimantIdentifyingFeatures(claim.getIdentifyingFeatures());
            if (claim.getClaimant() != null) {
                response.setClaimantId(claim.getClaimant().getId());
                response.setClaimantName(claim.getClaimant().getName());
            }
        }

        if (report != null) {
            response.setItemReportId(report.getId());
            response.setItemTitle(report.getTitle());
            response.setItemCategory(report.getCategory());
            response.setItemLocation(report.getLocation());
            response.setItemPublicDescription(report.getDescription());
            response.setReportDate(report.getReportDate());
            response.setItemStatus(report.getStatus());
            response.setReporterPrivateDescription(report.getPrivateDescription());
            if (report.getReporter() != null) {
                response.setReporterId(report.getReporter().getId());
                response.setReporterName(report.getReporter().getName());
            }
        }

        response.setReporterEvidenceId(reporterEvidenceId);
        response.setClaimantEvidenceId(claimantEvidenceId);

        return response;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public ClaimStatus getClaimStatus() {
        return claimStatus;
    }

    public void setClaimStatus(ClaimStatus claimStatus) {
        this.claimStatus = claimStatus;
    }

    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
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

    public String getItemCategory() {
        return itemCategory;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    public String getItemPublicDescription() {
        return itemPublicDescription;
    }

    public void setItemPublicDescription(String itemPublicDescription) {
        this.itemPublicDescription = itemPublicDescription;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public ItemStatus getItemStatus() {
        return itemStatus;
    }

    public void setItemStatus(ItemStatus itemStatus) {
        this.itemStatus = itemStatus;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterPrivateDescription() {
        return reporterPrivateDescription;
    }

    public void setReporterPrivateDescription(String reporterPrivateDescription) {
        this.reporterPrivateDescription = reporterPrivateDescription;
    }

    public Long getReporterEvidenceId() {
        return reporterEvidenceId;
    }

    public void setReporterEvidenceId(Long reporterEvidenceId) {
        this.reporterEvidenceId = reporterEvidenceId;
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

    public String getClaimantDescription() {
        return claimantDescription;
    }

    public void setClaimantDescription(String claimantDescription) {
        this.claimantDescription = claimantDescription;
    }

    public String getClaimantIdentifyingFeatures() {
        return claimantIdentifyingFeatures;
    }

    public void setClaimantIdentifyingFeatures(String claimantIdentifyingFeatures) {
        this.claimantIdentifyingFeatures = claimantIdentifyingFeatures;
    }

    public Long getClaimantEvidenceId() {
        return claimantEvidenceId;
    }

    public void setClaimantEvidenceId(Long claimantEvidenceId) {
        this.claimantEvidenceId = claimantEvidenceId;
    }
}

