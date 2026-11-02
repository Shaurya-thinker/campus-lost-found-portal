package com.campus.lostfound.service;

import com.campus.lostfound.entity.Claim;
import com.campus.lostfound.entity.EvidenceFile;
import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.ClaimStatus;
import com.campus.lostfound.entity.enums.EvidenceType;
import com.campus.lostfound.entity.enums.ItemStatus;
import com.campus.lostfound.entity.enums.ReportType;
import com.campus.lostfound.entity.enums.Role;
import com.campus.lostfound.repository.ClaimRepository;
import com.campus.lostfound.repository.EvidenceFileRepository;
import com.campus.lostfound.repository.ItemReportRepository;
import com.campus.lostfound.repository.UserRepository;
import com.campus.lostfound.security.UserPrincipal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    @Mock
    private EvidenceFileRepository evidenceRepository;

    @Mock
    private EvidenceStorageService storageService;

    @Mock
    private EvidenceValidator validator;

    @Mock
    private ItemReportRepository reportRepository;

    @Mock
    private ClaimRepository claimRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EvidenceService evidenceService;

    private User reporter;
    private User claimant;
    private User otherStudent;
    private User admin;

    private UserPrincipal reporterPrincipal;
    private UserPrincipal claimantPrincipal;
    private UserPrincipal otherStudentPrincipal;
    private UserPrincipal adminPrincipal;

    private ItemReport foundReport;
    private Claim claim;
    private EvidenceFile reporterEvidence;
    private EvidenceFile claimantEvidence;

    @BeforeEach
    void setUp() {
        reporter = new User(1L, "Alice Smith", "alice@campus.edu");
        reporter.setCollegeUid("24BCS10001");
        reporter.setRole(Role.STUDENT);

        claimant = new User(2L, "Bob Jones", "bob@campus.edu");
        claimant.setCollegeUid("24BCS10002");
        claimant.setRole(Role.STUDENT);

        otherStudent = new User(3L, "Charlie Brown", "charlie@campus.edu");
        otherStudent.setCollegeUid("24BCS10003");
        otherStudent.setRole(Role.STUDENT);

        admin = new User(99L, "Staff Admin", "admin@campus.edu");
        admin.setCollegeUid("ADMIN001");
        admin.setRole(Role.ADMIN);

        reporterPrincipal = UserPrincipal.create(reporter);
        claimantPrincipal = UserPrincipal.create(claimant);
        otherStudentPrincipal = UserPrincipal.create(otherStudent);
        adminPrincipal = UserPrincipal.create(admin);

        foundReport = new ItemReport(10L, "Keys", "Ring with 3 keys", "Car key has blue fob", "Gym",
                ReportType.FOUND, LocalDate.now(), ItemStatus.REPORTED, reporter);

        claim = new Claim(20L, foundReport, claimant, "My house and car keys", "Blue Nissan fob",
                LocalDate.now(), ClaimStatus.PENDING);

        reporterEvidence = new EvidenceFile("key-123.jpg", EvidenceType.REPORTER_PRIVATE, foundReport,
                null, reporter, "keys.jpg", "image/jpeg", 1024L, LocalDateTime.now());
        reporterEvidence.setId(101L);

        claimantEvidence = new EvidenceFile("key-456.jpg", EvidenceType.CLAIMANT_PRIVATE, null,
                claim, claimant, "proof.jpg", "image/jpeg", 2048L, LocalDateTime.now());
        claimantEvidence.setId(202L);
    }

    @Test
    @DisplayName("Reporter can successfully upload private evidence for their FOUND report")
    void testUploadReportEvidence_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "keys.jpg", "image/jpeg", new byte[100]);

        when(reportRepository.findById(10L)).thenReturn(Optional.of(foundReport));
        doNothing().when(validator).validateImage(file);
        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(storageService.store(any(), anyString(), anyString(), anyLong())).thenReturn("saved-key.jpg");
        when(evidenceRepository.save(any(EvidenceFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvidenceFile saved = evidenceService.uploadReportEvidence(10L, file, reporterPrincipal);

        assertNotNull(saved);
        assertEquals(EvidenceType.REPORTER_PRIVATE, saved.getEvidenceType());
        assertEquals("saved-key.jpg", saved.getStorageKey());
        assertEquals(foundReport, saved.getItemReport());
        assertEquals(reporter, saved.getUploadedBy());
    }

    @Test
    @DisplayName("Non-reporter student cannot upload evidence to another student's report")
    void testUploadReportEvidence_ForbiddenForOtherStudent() {
        MockMultipartFile file = new MockMultipartFile("file", "fake.jpg", "image/jpeg", new byte[100]);
        when(reportRepository.findById(10L)).thenReturn(Optional.of(foundReport));

        assertThrows(AccessDeniedException.class,
                () -> evidenceService.uploadReportEvidence(10L, file, otherStudentPrincipal));
        verify(evidenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Claimant can successfully upload private ownership evidence for their claim")
    void testUploadClaimEvidence_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "proof.jpg", "image/jpeg", new byte[200]);

        when(claimRepository.findById(20L)).thenReturn(Optional.of(claim));
        doNothing().when(validator).validateImage(file);
        when(userRepository.findById(2L)).thenReturn(Optional.of(claimant));
        when(storageService.store(any(), anyString(), anyString(), anyLong())).thenReturn("claim-proof.jpg");
        when(evidenceRepository.save(any(EvidenceFile.class))).thenAnswer(invocation -> invocation.getArgument(0));

        EvidenceFile saved = evidenceService.uploadClaimEvidence(20L, file, claimantPrincipal);

        assertNotNull(saved);
        assertEquals(EvidenceType.CLAIMANT_PRIVATE, saved.getEvidenceType());
        assertEquals(claim, saved.getClaim());
        assertEquals(claimant, saved.getUploadedBy());
    }

    @Test
    @DisplayName("Non-claimant student cannot upload evidence to another student's claim")
    void testUploadClaimEvidence_ForbiddenForOtherStudent() {
        MockMultipartFile file = new MockMultipartFile("file", "fake.jpg", "image/jpeg", new byte[100]);
        when(claimRepository.findById(20L)).thenReturn(Optional.of(claim));

        assertThrows(AccessDeniedException.class,
                () -> evidenceService.uploadClaimEvidence(20L, file, otherStudentPrincipal));
        verify(evidenceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Reporter can view their own report evidence")
    void testAccessEvidence_ReporterAllowed() {
        when(evidenceRepository.findById(101L)).thenReturn(Optional.of(reporterEvidence));
        when(storageService.loadAsResource("key-123.jpg")).thenReturn(new ByteArrayResource(new byte[10]));

        Resource resource = evidenceService.loadEvidenceResource(101L, reporterPrincipal);
        assertNotNull(resource);
    }

    @Test
    @DisplayName("Claimant can view their own claim evidence")
    void testAccessEvidence_ClaimantAllowed() {
        when(evidenceRepository.findById(202L)).thenReturn(Optional.of(claimantEvidence));
        when(storageService.loadAsResource("key-456.jpg")).thenReturn(new ByteArrayResource(new byte[10]));

        Resource resource = evidenceService.loadEvidenceResource(202L, claimantPrincipal);
        assertNotNull(resource);
    }

    @Test
    @DisplayName("Admin can view both reporter and claimant evidence")
    void testAccessEvidence_AdminAllowed() {
        when(evidenceRepository.findById(101L)).thenReturn(Optional.of(reporterEvidence));
        when(storageService.loadAsResource("key-123.jpg")).thenReturn(new ByteArrayResource(new byte[10]));

        Resource resource = evidenceService.loadEvidenceResource(101L, adminPrincipal);
        assertNotNull(resource);
    }

    @Test
    @DisplayName("Claimant CANNOT access the reporter's private evidence (403 Access Denied)")
    void testAccessEvidence_ClaimantDeniedReporterEvidence() {
        when(evidenceRepository.findById(101L)).thenReturn(Optional.of(reporterEvidence));

        assertThrows(AccessDeniedException.class, () -> evidenceService.loadEvidenceResource(101L, claimantPrincipal));
    }

    @Test
    @DisplayName("Unrelated student CANNOT access another student's evidence (403 Access Denied)")
    void testAccessEvidence_OtherStudentDenied() {
        when(evidenceRepository.findById(202L)).thenReturn(Optional.of(claimantEvidence));

        assertThrows(AccessDeniedException.class,
                () -> evidenceService.loadEvidenceResource(202L, otherStudentPrincipal));
    }

    @Test
    @DisplayName("Unauthenticated request throws AccessDeniedException")
    void testAccessEvidence_Unauthenticated() {
        assertThrows(AccessDeniedException.class, () -> evidenceService.loadEvidenceResource(101L, null));
    }

    @Test
    @DisplayName("Uploader can delete their own evidence")
    void testDeleteEvidence_UploaderAllowed() {
        when(evidenceRepository.findById(101L)).thenReturn(Optional.of(reporterEvidence));
        doNothing().when(storageService).delete("key-123.jpg");
        doNothing().when(evidenceRepository).delete(reporterEvidence);

        assertDoesNotThrow(() -> evidenceService.deleteEvidence(101L, reporterPrincipal));
        verify(storageService).delete("key-123.jpg");
        verify(evidenceRepository).delete(reporterEvidence);
    }

    @Test
    @DisplayName("Non-uploader student cannot delete another student's evidence")
    void testDeleteEvidence_ForbiddenForOther() {
        when(evidenceRepository.findById(101L)).thenReturn(Optional.of(reporterEvidence));

        assertThrows(AccessDeniedException.class, () -> evidenceService.deleteEvidence(101L, otherStudentPrincipal));
        verify(storageService, never()).delete(anyString());
    }
}
