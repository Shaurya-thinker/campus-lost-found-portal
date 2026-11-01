package com.campus.lostfound.repository;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for Claim entity operations.
 */
@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {

    /**
     * Finds all claims submitted for a specific item report.
     *
     * @param itemReportId ID of the target item report
     * @return list of claims for that report
     */
    List<Claim> findByItemReportId(Long itemReportId);

    /**
     * Finds all claims submitted by a specific user.
     *
     * @param claimantId ID of the claiming user
     * @return list of claims submitted by that user
     */
    List<Claim> findByClaimantId(Long claimantId);

    /**
     * Finds all claims matching a given status (e.g., PENDING, APPROVED, REJECTED).
     *
     * @param claimStatus status of the claim
     * @return list of claims matching the status
     */
    List<Claim> findByClaimStatus(ClaimStatus claimStatus);

    /**
     * Checks if a user has already submitted a claim for a given item report.
     *
     * @param itemReportId ID of the item report
     * @param claimantId ID of the claimant user
     * @return true if a claim already exists for this pair, false otherwise
     */
    boolean existsByItemReportIdAndClaimantId(Long itemReportId, Long claimantId);
}

