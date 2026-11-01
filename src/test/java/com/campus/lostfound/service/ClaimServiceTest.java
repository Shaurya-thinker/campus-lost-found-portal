package com.campus.lostfound.service;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.ClaimStatus;
import com.campus.lostfound.entity.enums.ItemStatus;
import com.campus.lostfound.entity.enums.ReportType;
import com.campus.lostfound.exception.BusinessRuleException;
import com.campus.lostfound.exception.DuplicateResourceException;
import com.campus.lostfound.repository.ClaimRepository;
import com.campus.lostfound.repository.ItemReportRepository;
import com.campus.lostfound.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClaimServiceTest {

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private ItemReportRepository itemReportRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ClaimService claimService;

    private User reporter;
    private User claimant;
    private ItemReport foundReport;
    private Claim pendingClaim;

    @BeforeEach
    void setUp() {
        reporter = new User(1L, "Alice Smith", "alice.smith@campus.edu");
        claimant = new User(2L, "Bob Jones", "bob.jones@campus.edu");
        foundReport = new ItemReport(10L, "Dell Laptop Bag", "Blue bag", "Electronics",
                "Library", ReportType.FOUND, LocalDate.now(), ItemStatus.REPORTED, reporter);
        pendingClaim = new Claim(100L, foundReport, claimant, "Contains my mouse",
                LocalDate.now(), ClaimStatus.PENDING);
    }

    @Test
    @DisplayName("Should successfully submit a claim in PENDING status")
    void testSubmitClaim_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(claimant));
        when(itemReportRepository.findById(10L)).thenReturn(Optional.of(foundReport));
        when(claimRepository.existsByItemReportIdAndClaimantId(10L, 2L)).thenReturn(false);
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Claim submitted = claimService.submitClaim(10L, 2L, "Contains my mouse");

        assertNotNull(submitted);
        assertEquals(ClaimStatus.PENDING, submitted.getClaimStatus());
        assertEquals(foundReport, submitted.getItemReport());
        assertEquals(claimant, submitted.getClaimant());
    }

    @Test
    @DisplayName("Should reject claim when item report type is LOST")
    void testSubmitClaim_CannotClaimLostItem() {
        foundReport.setReportType(ReportType.LOST);
        when(userRepository.findById(2L)).thenReturn(Optional.of(claimant));
        when(itemReportRepository.findById(10L)).thenReturn(Optional.of(foundReport));

        assertThrows(BusinessRuleException.class, () -> claimService.submitClaim(10L, 2L, "My lost item"));
    }

    @Test
    @DisplayName("Should reject claim when item status is not REPORTED")
    void testSubmitClaim_CannotClaimAlreadyClaimedItem() {
        foundReport.setStatus(ItemStatus.CLAIMED);
        when(userRepository.findById(2L)).thenReturn(Optional.of(claimant));
        when(itemReportRepository.findById(10L)).thenReturn(Optional.of(foundReport));

        assertThrows(BusinessRuleException.class, () -> claimService.submitClaim(10L, 2L, "Description"));
    }

    @Test
    @DisplayName("Should reject claim when user attempts to claim their own report")
    void testSubmitClaim_CannotClaimOwnReport() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(itemReportRepository.findById(10L)).thenReturn(Optional.of(foundReport));

        assertThrows(BusinessRuleException.class, () -> claimService.submitClaim(10L, 1L, "Claiming own item"));
    }

    @Test
    @DisplayName("Should reject duplicate claim by the same claimant for the same item")
    void testSubmitClaim_DuplicateClaim() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(claimant));
        when(itemReportRepository.findById(10L)).thenReturn(Optional.of(foundReport));
        when(claimRepository.existsByItemReportIdAndClaimantId(10L, 2L)).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () -> claimService.submitClaim(10L, 2L, "Second attempt"));
    }

    @Test
    @DisplayName("Should approve PENDING claim and transition item status to CLAIMED")
    void testApproveClaim_Success() {
        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Claim approved = claimService.approveClaim(100L);

        assertEquals(ClaimStatus.APPROVED, approved.getClaimStatus());
        assertEquals(ItemStatus.CLAIMED, foundReport.getStatus());
        verify(itemReportRepository).save(foundReport);
        verify(claimRepository).save(pendingClaim);
    }

    @Test
    @DisplayName("Should reject approving claim if item is already CLAIMED")
    void testApproveClaim_ItemAlreadyClaimed() {
        foundReport.setStatus(ItemStatus.CLAIMED);
        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));

        assertThrows(BusinessRuleException.class, () -> claimService.approveClaim(100L));
    }

    @Test
    @DisplayName("Should reject a PENDING claim")
    void testRejectClaim_Success() {
        when(claimRepository.findById(100L)).thenReturn(Optional.of(pendingClaim));
        when(claimRepository.save(any(Claim.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Claim rejected = claimService.rejectClaim(100L);

        assertEquals(ClaimStatus.REJECTED, rejected.getClaimStatus());
        verify(claimRepository).save(pendingClaim);
    }
}

