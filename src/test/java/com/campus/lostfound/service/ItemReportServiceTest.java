package com.campus.lostfound.service;

import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.ItemStatus;
import com.campus.lostfound.entity.enums.ReportType;
import com.campus.lostfound.exception.BusinessRuleException;
import com.campus.lostfound.exception.ResourceNotFoundException;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemReportServiceTest {

    @Mock
    private ItemReportRepository itemReportRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ItemReportService itemReportService;

    private User reporter;
    private ItemReport sampleReport;

    @BeforeEach
    void setUp() {
        reporter = new User(1L, "Alice Smith", "alice.smith@campus.edu");
        sampleReport = new ItemReport(1L, "Laptop Bag", "Blue bag", "Electronics",
                "Library", ReportType.FOUND, LocalDate.now(), ItemStatus.REPORTED, reporter);
    }

    @Test
    @DisplayName("Should successfully create a FOUND report with private description and default REPORTED status")
    void testCreateReport_Success() {
        ItemReport input = new ItemReport();
        input.setTitle("Laptop Bag");
        input.setDescription("Blue bag");
        input.setPrivateDescription("Engraved initials AS on buckle and silver keychain inside");
        input.setCategory("Electronics");
        input.setLocation("Library");
        input.setReportType(ReportType.FOUND);

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(itemReportRepository.save(any(ItemReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemReport created = itemReportService.createReport(input, 1L);

        assertNotNull(created);
        assertEquals(ItemStatus.REPORTED, created.getStatus());
        assertEquals(reporter, created.getReporter());
        assertEquals("Engraved initials AS on buckle and silver keychain inside", created.getPrivateDescription());
        assertNotNull(created.getReportDate());
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when FOUND report lacks private distinguishing details")
    void testCreateReport_FoundReportMissingPrivateDescription() {
        ItemReport input = new ItemReport();
        input.setTitle("Laptop Bag");
        input.setDescription("Blue bag");
        input.setPrivateDescription(""); // empty private description
        input.setCategory("Electronics");
        input.setLocation("Library");
        input.setReportType(ReportType.FOUND);

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));

        assertThrows(BusinessRuleException.class, () -> itemReportService.createReport(input, 1L));
        verify(itemReportRepository, never()).save(any(ItemReport.class));
    }

    @Test
    @DisplayName("Should successfully create LOST report without requiring private description")
    void testCreateReport_LostReportSucceedsWithoutPrivateDescription() {
        ItemReport input = new ItemReport();
        input.setTitle("Water Bottle");
        input.setDescription("Hydro Flask");
        input.setCategory("Personal Items");
        input.setLocation("Gym");
        input.setReportType(ReportType.LOST);

        when(userRepository.findById(1L)).thenReturn(Optional.of(reporter));
        when(itemReportRepository.save(any(ItemReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemReport created = itemReportService.createReport(input, 1L);

        assertNotNull(created);
        assertEquals(ItemStatus.REPORTED, created.getStatus());
        verify(itemReportRepository).save(any(ItemReport.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when reporter does not exist")
    void testCreateReport_ReporterNotFound() {
        ItemReport input = new ItemReport();
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> itemReportService.createReport(input, 99L));
    }

    @Test
    @DisplayName("Should mark an item as RETURNED when it is in CLAIMED status")
    void testMarkAsReturned_Success() {
        sampleReport.setStatus(ItemStatus.CLAIMED);
        when(itemReportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));
        when(itemReportRepository.save(any(ItemReport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemReport returned = itemReportService.markAsReturned(1L);

        assertEquals(ItemStatus.RETURNED, returned.getStatus());
        verify(itemReportRepository).save(sampleReport);
    }

    @Test
    @DisplayName("Should throw BusinessRuleException when marking item as RETURNED if not in CLAIMED status")
    void testMarkAsReturned_InvalidPriorStatus() {
        sampleReport.setStatus(ItemStatus.REPORTED);
        when(itemReportRepository.findById(1L)).thenReturn(Optional.of(sampleReport));

        assertThrows(BusinessRuleException.class, () -> itemReportService.markAsReturned(1L));
    }
}
