package com.campus.lostfound.service;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.ClaimStatus;
import com.campus.lostfound.entity.enums.ItemStatus;
import com.campus.lostfound.entity.enums.ReportType;
import com.campus.lostfound.exception.BusinessRuleException;
import com.campus.lostfound.exception.DuplicateResourceException;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.repository.ClaimRepository;
import com.campus.lostfound.repository.ItemReportRepository;
import com.campus.lostfound.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service managing claims submitted for found items, verifying ownership, and handling approvals.
 */
@Service
@Transactional(readOnly = true)
public class ClaimService {

    private final ClaimRepository claimRepository;
    private final ItemReportRepository itemReportRepository;
    private final UserRepository userRepository;

    public ClaimService(ClaimRepository claimRepository,
                        ItemReportRepository itemReportRepository,
                        UserRepository userRepository) {
        this.claimRepository = claimRepository;
        this.itemReportRepository = itemReportRepository;
        this.userRepository = userRepository;
    }

    /**
     * Submits a claim for an item report after enforcing all business rules:
     * 1. Claimant must be an existing user.
     * 2. Item report must exist.
     * 3. Only a FOUND report with REPORTED status can receive a claim.
     * 4. A user cannot claim an item they reported themselves.
     * 5. The same user cannot submit duplicate claims for the same report.
     *
     * @param itemReportId ID of the item being claimed
     * @param claimantId   ID of the claimant user
     * @param description  ownership-verification description
     * @return persisted Claim
     */
    @Transactional
    public Claim submitClaim(Long itemReportId, Long claimantId, String description) {
        User claimant = userRepository.findById(claimantId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + claimantId));

        ItemReport itemReport = itemReportRepository.findById(itemReportId)
                .orElseThrow(() -> new ResourceNotFoundException("Item report not found with id: " + itemReportId));

        if (itemReport.getReportType() != ReportType.FOUND) {
            throw new BusinessRuleException("Claims can only be submitted for FOUND items.");
        }

        if (itemReport.getStatus() != ItemStatus.REPORTED) {
            throw new BusinessRuleException("Claims can only be submitted for items with REPORTED status.");
        }

        if (itemReport.getReporter().getId().equals(claimant.getId())) {
            throw new BusinessRuleException("A user cannot submit a claim for an item they reported themselves.");
        }

        if (claimRepository.existsByItemReportIdAndClaimantId(itemReportId, claimantId)) {
            throw new DuplicateResourceException("User has already submitted a claim for this item report.");
        }

        Claim claim = new Claim();
        claim.setItemReport(itemReport);
        claim.setClaimant(claimant);
        claim.setDescription(description);
        claim.setClaimDate(LocalDate.now());
        claim.setClaimStatus(ClaimStatus.PENDING);

        return claimRepository.save(claim);
    }

    /**
     * Retrieves a claim by its primary key ID.
     *
     * @param id claim ID
     * @return Claim entity
     * @throws ResourceNotFoundException if claim is not found
     */
    public Claim getClaimById(Long id) {
        return claimRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + id));
    }

    /**
     * Retrieves all claims filed against a specific item report.
     *
     * @param itemReportId ID of the item report
     * @return list of claims
     * @throws ResourceNotFoundException if item report does not exist
     */
    public List<Claim> getClaimsByItemReportId(Long itemReportId) {
        if (!itemReportRepository.existsById(itemReportId)) {
            throw new ResourceNotFoundException("Item report not found with id: " + itemReportId);
        }
        return claimRepository.findByItemReportId(itemReportId);
    }

    /**
     * Retrieves all claims submitted by a specific user.
     *
     * @param claimantId ID of the claimant
     * @return list of claims submitted by user
     * @throws ResourceNotFoundException if claimant user does not exist
     */
    public List<Claim> getClaimsByClaimantId(Long claimantId) {
        if (!userRepository.existsById(claimantId)) {
            throw new ResourceNotFoundException("User not found with id: " + claimantId);
        }
        return claimRepository.findByClaimantId(claimantId);
    }

    /**
     * Retrieves all claims matching a given status (PENDING, APPROVED, REJECTED).
     *
     * @param status claim status
     * @return list of matching claims
     */
    public List<Claim> getClaimsByStatus(ClaimStatus status) {
        return claimRepository.findByClaimStatus(status);
    }

    /**
     * Approves a pending claim and marks the corresponding item report as CLAIMED.
     * Enforces:
     * - Only a PENDING claim can be approved.
     * - Once an item has an approved claim (no longer in REPORTED status), subsequent claims cannot be approved.
     *
     * @param claimId ID of the claim to approve
     * @return updated Claim
     * @throws ResourceNotFoundException if claim is not found
     * @throws BusinessRuleException     if claim is not pending or item is already claimed/returned
     */
    @Transactional
    public Claim approveClaim(Long claimId) {
        Claim claim = getClaimById(claimId);

        if (claim.getClaimStatus() != ClaimStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING claims can be approved. Current status: " + claim.getClaimStatus());
        }

        ItemReport itemReport = claim.getItemReport();
        if (itemReport.getStatus() != ItemStatus.REPORTED) {
            throw new BusinessRuleException("Cannot approve this claim: The associated item is no longer in REPORTED status (current: "
                    + itemReport.getStatus() + ").");
        }

        claim.setClaimStatus(ClaimStatus.APPROVED);
        itemReport.setStatus(ItemStatus.CLAIMED);

        itemReportRepository.save(itemReport);
        return claimRepository.save(claim);
    }

    /**
     * Rejects a pending claim.
     * Enforces that only a PENDING claim can be rejected.
     *
     * @param claimId ID of the claim to reject
     * @return updated Claim
     * @throws ResourceNotFoundException if claim is not found
     * @throws BusinessRuleException     if claim is not pending
     */
    @Transactional
    public Claim rejectClaim(Long claimId) {
        Claim claim = getClaimById(claimId);

        if (claim.getClaimStatus() != ClaimStatus.PENDING) {
            throw new BusinessRuleException("Only PENDING claims can be rejected. Current status: " + claim.getClaimStatus());
        }

        claim.setClaimStatus(ClaimStatus.REJECTED);
        return claimRepository.save(claim);
    }
}

