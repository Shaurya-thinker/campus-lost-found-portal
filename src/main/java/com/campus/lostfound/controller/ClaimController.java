package com.campus.lostfound.controller;

import com.campus.lostfound.dto.ClaimResponse;
import com.campus.lostfound.dto.ClaimSubmissionRequest;
import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.enums.ClaimStatus;
import com.campus.lostfound.exception.BusinessRuleException;
import com.campus.lostfound.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller managing claim submissions, evaluations, and status tracking.
 * Base path: /api/claims
 */
@RestController
@RequestMapping("/api/claims")
public class ClaimController {

    private final ClaimService claimService;

    public ClaimController(ClaimService claimService) {
        this.claimService = claimService;
    }

    /**
     * Submits a claim for a found item report.
     * POST /api/claims
     */
    @PostMapping
    public ResponseEntity<ClaimResponse> submitClaim(@Valid @RequestBody ClaimSubmissionRequest request) {
        Claim claim = claimService.submitClaim(
                request.getItemReportId(),
                request.getClaimantId(),
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ClaimResponse.fromEntity(claim));
    }

    /**
     * Retrieves an individual claim by its ID.
     * GET /api/claims/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(@PathVariable Long id) {
        Claim claim = claimService.getClaimById(id);
        return ResponseEntity.ok(ClaimResponse.fromEntity(claim));
    }

    /**
     * Retrieves claims filtered by reportId, claimantId, or status.
     * Enforces mutually exclusive filtering to avoid ambiguous queries.
     * GET /api/claims?reportId=...
     * GET /api/claims?claimantId=...
     * GET /api/claims?status=PENDING
     */
    @GetMapping
    public ResponseEntity<List<ClaimResponse>> getClaims(
            @RequestParam(required = false) Long reportId,
            @RequestParam(required = false) Long claimantId,
            @RequestParam(required = false) ClaimStatus status) {

        int filterCount = (reportId != null ? 1 : 0) + (claimantId != null ? 1 : 0) + (status != null ? 1 : 0);

        if (filterCount > 1) {
            throw new BusinessRuleException("Only one filter parameter ('reportId', 'claimantId', or 'status') can be specified at a time.");
        }
        if (filterCount == 0) {
            throw new BusinessRuleException("A query parameter ('reportId', 'claimantId', or 'status') must be provided to filter claims.");
        }

        List<Claim> claims;
        if (reportId != null) {
            claims = claimService.getClaimsByItemReportId(reportId);
        } else if (claimantId != null) {
            claims = claimService.getClaimsByClaimantId(claimantId);
        } else {
            claims = claimService.getClaimsByStatus(status);
        }

        List<ClaimResponse> response = claims.stream()
                .map(ClaimResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Approves a pending claim and marks the item report as CLAIMED.
     * PATCH /api/claims/{id}/approve
     */
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ClaimResponse> approveClaim(@PathVariable Long id) {
        Claim approved = claimService.approveClaim(id);
        return ResponseEntity.ok(ClaimResponse.fromEntity(approved));
    }

    /**
     * Rejects a pending claim.
     * PATCH /api/claims/{id}/reject
     */
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ClaimResponse> rejectClaim(@PathVariable Long id) {
        Claim rejected = claimService.rejectClaim(id);
        return ResponseEntity.ok(ClaimResponse.fromEntity(rejected));
    }
}

