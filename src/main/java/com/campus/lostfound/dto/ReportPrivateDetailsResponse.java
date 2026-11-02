package com.campus.lostfound.dto;

/**
 * Confidential details for an item report, accessible only to the reporting user or an administrator.
 */
public class ReportPrivateDetailsResponse {

    private Long reportId;
    private String privateDescription;
    private Long evidenceId;
    private String originalFilename;
    private Long fileSize;
    private String contentType;

    public ReportPrivateDetailsResponse() {
    }

    public ReportPrivateDetailsResponse(Long reportId, String privateDescription,
                                        Long evidenceId, String originalFilename,
                                        Long fileSize, String contentType) {
        this.reportId = reportId;
        this.privateDescription = privateDescription;
        this.evidenceId = evidenceId;
        this.originalFilename = originalFilename;
        this.fileSize = fileSize;
        this.contentType = contentType;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getPrivateDescription() {
        return privateDescription;
    }

    public void setPrivateDescription(String privateDescription) {
        this.privateDescription = privateDescription;
    }

    public Long getEvidenceId() {
        return evidenceId;
    }

    public void setEvidenceId(Long evidenceId) {
        this.evidenceId = evidenceId;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}

