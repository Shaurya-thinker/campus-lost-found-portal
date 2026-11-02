package com.campus.lostfound.controller;

import com.campus.lostfound.dto.EvidenceResponse;
import com.campus.lostfound.entity.EvidenceFile;
import com.campus.lostfound.security.UserPrincipal;
import com.campus.lostfound.service.EvidenceService;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.concurrent.TimeUnit;

/**
 * Controller providing secure endpoints for uploading and streaming private visual evidence.
 * All evidence is referenced by opaque numeric database IDs, never physical filesystem paths.
 */
@RestController
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    /**
     * Uploads private evidence for a FOUND item report.
     * Accessible by the reporter or an administrator.
     * POST /api/reports/{reportId}/evidence
     */
    @PostMapping(value = "/api/reports/{reportId}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceResponse> uploadReportEvidence(
            @PathVariable Long reportId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required to upload evidence.");
        }

        EvidenceFile saved = evidenceService.uploadReportEvidence(reportId, file, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(EvidenceResponse.fromEntity(saved));
    }

    /**
     * Uploads private ownership evidence for a claim.
     * Accessible by the claimant or an administrator.
     * POST /api/claims/{claimId}/evidence
     */
    @PostMapping(value = "/api/claims/{claimId}/evidence", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<EvidenceResponse> uploadClaimEvidence(
            @PathVariable Long claimId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required to upload evidence.");
        }

        EvidenceFile saved = evidenceService.uploadClaimEvidence(claimId, file, principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(EvidenceResponse.fromEntity(saved));
    }

    /**
     * Streams private evidence by its opaque ID.
     * Strictly verifies that the authenticated user is the uploader, the reporter (for report evidence),
     * the claimant (for claim evidence), or an authorized administrator.
     * GET /api/evidence/{id}
     */
    @GetMapping("/api/evidence/{id}")
    public ResponseEntity<Resource> getEvidence(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required to view evidence.");
        }

        EvidenceFile metadata = evidenceService.getEvidenceFile(id, principal);
        Resource resource = evidenceService.loadEvidenceResource(id, principal);

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (metadata.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(metadata.getContentType());
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"evidence-" + id + "\"")
                .cacheControl(CacheControl.noStore().mustRevalidate().cachePrivate())
                .body(resource);
    }

    /**
     * Deletes evidence file.
     * DELETE /api/evidence/{id}
     */
    @DeleteMapping("/api/evidence/{id}")
    public ResponseEntity<Void> deleteEvidence(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required to delete evidence.");
        }

        evidenceService.deleteEvidence(id, principal);
        return ResponseEntity.noContent().build();
    }
}

