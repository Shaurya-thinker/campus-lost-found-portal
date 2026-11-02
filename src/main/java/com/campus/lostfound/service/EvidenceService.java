package com.campus.lostfound.service;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.EvidenceFile;
import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.EvidenceType;
import com.campus.lostfound.entity.enums.Role;
import com.campus.lostfound.exception.BusinessRuleException;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.repository.ClaimRepository;
import com.campus.lostfound.repository.EvidenceFileRepository;
import com.campus.lostfound.repository.ItemReportRepository;
import com.campus.lostfound.repository.UserRepository;
import com.campus.lostfound.security.UserPrincipal;
import org.springframework.core.io.Resource;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

/**
 * Service managing private visual evidence upload, access control, and retrieval.
 */
@Service
@Transactional(readOnly = true)
public class EvidenceService {

    private final EvidenceFileRepository evidenceRepository;
    private final EvidenceStorageService storageService;
    private final EvidenceValidator validator;
    private final ItemReportRepository reportRepository;
    private final ClaimRepository claimRepository;
    private final UserRepository userRepository;

    public EvidenceService(EvidenceFileRepository evidenceRepository,
                           EvidenceStorageService storageService,
                           EvidenceValidator validator,
                           ItemReportRepository reportRepository,
                           ClaimRepository claimRepository,
                           UserRepository userRepository) {
        this.evidenceRepository = evidenceRepository;
        this.storageService = storageService;
        this.validator = validator;
        this.reportRepository = reportRepository;
        this.claimRepository = claimRepository;
        this.userRepository = userRepository;
    }

    /**
     * Uploads private photographic evidence for a FOUND report by the reporter or admin.
     */
    @Transactional
    public EvidenceFile uploadReportEvidence(Long reportId, MultipartFile file, UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required to upload evidence.");
        }

        ItemReport report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("Item report not found with id: " + reportId));

        if (principal.getRole() != Role.ADMIN && !Objects.equals(report.getReporter().getId(), principal.getId())) {
            throw new AccessDeniedException("You are not authorized to upload evidence for this report.");
        }

        validator.validateImage(file);

        User uploader = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        String storageKey;
        try {
            storageKey = storageService.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new BusinessRuleException("Failed to read evidence file stream: " + e.getMessage());
        }

        EvidenceFile evidence = new EvidenceFile();
        evidence.setStorageKey(storageKey);
        evidence.setEvidenceType(EvidenceType.REPORTER_PRIVATE);
        evidence.setItemReport(report);
        evidence.setUploadedBy(uploader);
        evidence.setOriginalFilename(sanitizeOriginalFilename(file.getOriginalFilename()));
        evidence.setContentType(file.getContentType());
        evidence.setFileSize(file.getSize());
        evidence.setCreatedAt(LocalDateTime.now());

        return evidenceRepository.save(evidence);
    }

    /**
     * Uploads private ownership evidence for a claim by the claimant or admin.
     */
    @Transactional
    public EvidenceFile uploadClaimEvidence(Long claimId, MultipartFile file, UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required to upload evidence.");
        }

        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new ResourceNotFoundException("Claim not found with id: " + claimId));

        if (principal.getRole() != Role.ADMIN && !Objects.equals(claim.getClaimant().getId(), principal.getId())) {
            throw new AccessDeniedException("You are not authorized to upload evidence for this claim.");
        }

        validator.validateImage(file);

        User uploader = userRepository.findById(principal.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + principal.getId()));

        String storageKey;
        try {
            storageKey = storageService.store(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), file.getSize());
        } catch (IOException e) {
            throw new BusinessRuleException("Failed to read evidence file stream: " + e.getMessage());
        }

        EvidenceFile evidence = new EvidenceFile();
        evidence.setStorageKey(storageKey);
        evidence.setEvidenceType(EvidenceType.CLAIMANT_PRIVATE);
        evidence.setClaim(claim);
        evidence.setUploadedBy(uploader);
        evidence.setOriginalFilename(sanitizeOriginalFilename(file.getOriginalFilename()));
        evidence.setContentType(file.getContentType());
        evidence.setFileSize(file.getSize());
        evidence.setCreatedAt(LocalDateTime.now());

        return evidenceRepository.save(evidence);
    }

    /**
     * Loads evidence resource after verifying fine-grained caller authorization.
     */
    public Resource loadEvidenceResource(Long evidenceId, UserPrincipal principal) {
        EvidenceFile evidence = getEvidenceFile(evidenceId, principal);
        return storageService.loadAsResource(evidence.getStorageKey());
    }

    /**
     * Retrieves an EvidenceFile entity after verifying access authorization.
     */
    public EvidenceFile getEvidenceFile(Long evidenceId, UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required to access evidence.");
        }

        EvidenceFile evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with id: " + evidenceId));

        verifyEvidenceAccess(evidence, principal);
        return evidence;
    }

    /**
     * Verifies that the principal is authorized to access the given evidence:
     * - Administrators can access all evidence.
     * - Uploader can access their own evidence.
     * - Reporters can access evidence on their report.
     * - Claimants can access evidence on their claim.
     */
    public void verifyEvidenceAccess(EvidenceFile evidence, UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required to access evidence.");
        }

        // 1. Administrator override
        if (principal.getRole() == Role.ADMIN) {
            return;
        }

        // 2. Direct uploader access
        if (evidence.getUploadedBy() != null && Objects.equals(evidence.getUploadedBy().getId(), principal.getId())) {
            return;
        }

        // 3. Reporter access to report's private evidence
        if (evidence.getEvidenceType() == EvidenceType.REPORTER_PRIVATE && evidence.getItemReport() != null) {
            if (Objects.equals(evidence.getItemReport().getReporter().getId(), principal.getId())) {
                return;
            }
        }

        // 4. Claimant access to claim's private evidence
        if (evidence.getEvidenceType() == EvidenceType.CLAIMANT_PRIVATE && evidence.getClaim() != null) {
            if (Objects.equals(evidence.getClaim().getClaimant().getId(), principal.getId())) {
                return;
            }
        }

        // Access denied for all other users (e.g. claimants attempting to view reporter evidence)
        throw new AccessDeniedException("You are not authorized to view this private evidence.");
    }

    /**
     * Deletes evidence metadata and storage file.
     */
    @Transactional
    public void deleteEvidence(Long evidenceId, UserPrincipal principal) {
        if (principal == null || principal.getId() == null) {
            throw new AccessDeniedException("Authentication required to delete evidence.");
        }

        EvidenceFile evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with id: " + evidenceId));

        if (principal.getRole() != Role.ADMIN && !Objects.equals(evidence.getUploadedBy().getId(), principal.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this evidence.");
        }

        storageService.delete(evidence.getStorageKey());
        evidenceRepository.delete(evidence);
    }

    public Optional<EvidenceFile> getReportEvidence(Long reportId) {
        return evidenceRepository.findFirstByItemReportIdAndEvidenceType(reportId, EvidenceType.REPORTER_PRIVATE);
    }

    public Optional<EvidenceFile> getClaimEvidence(Long claimId) {
        return evidenceRepository.findFirstByClaimIdAndEvidenceType(claimId, EvidenceType.CLAIMANT_PRIVATE);
    }

    private String sanitizeOriginalFilename(String name) {
        if (name == null || name.isBlank()) {
            return "evidence.jpg";
        }
        String clean = Paths.get(name).getFileName().toString();
        return clean.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}

