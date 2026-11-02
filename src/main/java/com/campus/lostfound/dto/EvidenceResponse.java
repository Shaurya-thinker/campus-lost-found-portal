package com.campus.lostfound.dto;

import com.campus.lostfound.entity.EvidenceFile;
import com.campus.lostfound.entity.enums.EvidenceType;

import java.time.LocalDateTime;

/**
 * Public/client-safe response payload for evidence metadata.
 * Never exposes raw filesystem paths, storage keys, or unauthenticated file URLs.
 */
public class EvidenceResponse {

    private Long id;
    private EvidenceType evidenceType;
    private Long itemReportId;
    private Long claimId;
    private Long uploadedById;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private LocalDateTime createdAt;

    public EvidenceResponse() {
    }

    public EvidenceResponse(Long id, EvidenceType evidenceType, Long itemReportId,
                            Long claimId, Long uploadedById, String originalFilename,
                            String contentType, Long fileSize, LocalDateTime createdAt) {
        this.id = id;
        this.evidenceType = evidenceType;
        this.itemReportId = itemReportId;
        this.claimId = claimId;
        this.uploadedById = uploadedById;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public static EvidenceResponse fromEntity(EvidenceFile entity) {
        if (entity == null) {
            return null;
        }
        return new EvidenceResponse(
                entity.getId(),
                entity.getEvidenceType(),
                entity.getItemReport() != null ? entity.getItemReport().getId() : null,
                entity.getClaim() != null ? entity.getClaim().getId() : null,
                entity.getUploadedBy() != null ? entity.getUploadedBy().getId() : null,
                entity.getOriginalFilename(),
                entity.getContentType(),
                entity.getFileSize(),
                entity.getCreatedAt()
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public EvidenceType getEvidenceType() {
        return evidenceType;
    }

    public void setEvidenceType(EvidenceType evidenceType) {
        this.evidenceType = evidenceType;
    }

    public Long getItemReportId() {
        return itemReportId;
    }

    public void setItemReportId(Long itemReportId) {
        this.itemReportId = itemReportId;
    }

    public Long getClaimId() {
        return claimId;
    }

    public void setClaimId(Long claimId) {
        this.claimId = claimId;
    }

    public Long getUploadedById() {
        return uploadedById;
    }

    public void setUploadedById(Long uploadedById) {
        this.uploadedById = uploadedById;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}

