package com.campus.lostfound.controller;

import com.campus.lostfound.dto.ClaimResponse;
import com.campus.lostfound.dto.ClaimSubmissionRequest;
import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.enums.ClaimStatus;
import com.campus.lostfound.entity.enums.Role;
import com.campus.lostfound.exception.BusinessRuleException;
import com.campus.lostfound.security.UserPrincipal;
import com.campus.lostfound.service.ClaimService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * Claimant identity is derived securely from the authenticated principal.
     * POST /api/claims
     */
    @PostMapping
    public ResponseEntity<ClaimResponse> submitClaim(
            @Valid @RequestBody ClaimSubmissionRequest request,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required to submit a claim.");
        }
        Long claimantId = principal.getId();
        Claim claim = claimService.submitClaim(
                request.getItemReportId(),
                claimantId,
                request.getDescription()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(ClaimResponse.fromEntity(claim));
    }

    /**
     * Retrieves an individual claim by its ID.
     * Students may only view their own claims; administrators may view any claim.
     * GET /api/claims/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ClaimResponse> getClaimById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {
        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view claims.");
        }
        Claim claim = claimService.getClaimById(id);
        if (principal.getRole() != Role.ADMIN && !claim.getClaimant().getId().equals(principal.getId())) {
            throw new AccessDeniedException("You are not authorized to view this claim.");
        }
        return ResponseEntity.ok(ClaimResponse.fromEntity(claim));
    }

    /**
     * Retrieves claims filtered by reportId, claimantId, or status.
     * Enforces mutually exclusive filtering to avoid ambiguous queries.
     * Non-admin students are strictly restricted to querying their own claims.
     * GET /api/claims?reportId=...
     * GET /api/claims?claimantId=...
     * GET /api/claims?status=PENDING
     */
    @GetMapping
    public ResponseEntity<List<ClaimResponse>> getClaims(
            @RequestParam(required = false) Long reportId,
            @RequestParam(required = false) Long claimantId,
            @RequestParam(required = false) ClaimStatus status,
            @AuthenticationPrincipal UserPrincipal principal) {

        if (principal == null) {
            throw new AccessDeniedException("Authentication required to view claims.");
        }

        boolean isAdmin = principal.getRole() == Role.ADMIN;

        // If user is a student, restrict query strictly to their own claimantId
        if (!isAdmin) {
            if (reportId != null || status != null || (claimantId != null && !claimantId.equals(principal.getId()))) {
                throw new AccessDeniedException("Students are only permitted to view their own claims.");
            }
            claimantId = principal.getId();
        }

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
     * Restricted to administrators.
     * PATCH /api/claims/{id}/approve
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/approve")
    public ResponseEntity<ClaimResponse> approveClaim(@PathVariable Long id) {
        Claim approved = claimService.approveClaim(id);
        return ResponseEntity.ok(ClaimResponse.fromEntity(approved));
    }

    /**
     * Rejects a pending claim.
     * Restricted to administrators.
     * PATCH /api/claims/{id}/reject
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/reject")
    public ResponseEntity<ClaimResponse> rejectClaim(@PathVariable Long id) {
        Claim rejected = claimService.rejectClaim(id);
        return ResponseEntity.ok(ClaimResponse.fromEntity(rejected));
    }
}

