package com.campus.lostfound.service;

import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.User;
import com.campus.lostfound.entity.enums.ItemStatus;
import com.campus.lostfound.entity.enums.ReportType;
import com.campus.lostfound.exception.BusinessRuleException;
import com.campus.lostfound.exception.ResourceNotFoundException;
import com.campus.lostfound.repository.ItemReportRepository;
import com.campus.lostfound.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Service managing item reports (lost and found) and search operations.
 */
@Service
@Transactional(readOnly = true)
public class ItemReportService {

    private final ItemReportRepository itemReportRepository;
    private final UserRepository userRepository;

    public ItemReportService(ItemReportRepository itemReportRepository, UserRepository userRepository) {
        this.itemReportRepository = itemReportRepository;
        this.userRepository = userRepository;
    }

    /**
     * Creates a new LOST or FOUND item report submitted by an existing user.
     *
     * @param report     item report details
     * @param reporterId ID of the user submitting the report
     * @return persisted ItemReport
     * @throws ResourceNotFoundException if the reporter user does not exist
     */
    @Transactional
    public ItemReport createReport(ItemReport report, Long reporterId) {
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + reporterId));

        report.setReporter(reporter);

        if (report.getReportDate() == null) {
            report.setReportDate(LocalDate.now());
        }
        if (report.getStatus() == null) {
            report.setStatus(ItemStatus.REPORTED);
        }

        return itemReportRepository.save(report);
    }

    /**
     * Retrieves an item report by its primary key ID.
     *
     * @param id report ID
     * @return ItemReport entity
     * @throws ResourceNotFoundException if report is not found
     */
    public ItemReport getReportById(Long id) {
        return itemReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item report not found with id: " + id));
    }

    /**
     * Retrieves all item reports in the system.
     *
     * @return list of all item reports
     */
    public List<ItemReport> getAllReports() {
        return itemReportRepository.findAll();
    }

    /**
     * Filters item reports by report type (LOST/FOUND) and/or status (REPORTED/CLAIMED/RETURNED).
     *
     * @param reportType optional filter by LOST or FOUND
     * @param status     optional filter by item status
     * @return list of matching reports
     */
    public List<ItemReport> getReportsByTypeAndStatus(ReportType reportType, ItemStatus status) {
        if (reportType != null && status != null) {
            return itemReportRepository.findByReportTypeAndStatus(reportType, status);
        } else if (reportType != null) {
            return itemReportRepository.findByReportType(reportType);
        } else if (status != null) {
            return itemReportRepository.findByStatus(status);
        }
        return itemReportRepository.findAll();
    }

    /**
     * Searches reports by category and/or location, ignoring case.
     *
     * @param category optional category search term
     * @param location optional location search term
     * @return list of matching reports
     */
    public List<ItemReport> searchReports(String category, String location) {
        boolean hasCategory = category != null && !category.trim().isEmpty();
        boolean hasLocation = location != null && !location.trim().isEmpty();

        if (hasCategory && hasLocation) {
            return itemReportRepository.findByCategoryIgnoreCaseAndLocationIgnoreCase(category.trim(), location.trim());
        } else if (hasCategory) {
            return itemReportRepository.findByCategoryIgnoreCase(category.trim());
        } else if (hasLocation) {
            return itemReportRepository.findByLocationIgnoreCase(location.trim());
        }
        return itemReportRepository.findAll();
    }

    /**
     * Retrieves all reports logged by a specific user.
     *
     * @param reporterId ID of the reporting user
     * @return list of reports by the user
     * @throws ResourceNotFoundException if user does not exist
     */
    public List<ItemReport> getReportsByReporterId(Long reporterId) {
        if (!userRepository.existsById(reporterId)) {
            throw new ResourceNotFoundException("User not found with id: " + reporterId);
        }
        return itemReportRepository.findByReporterId(reporterId);
    }

    /**
     * Updates editable details of an existing item report.
     *
     * @param id             report ID to update
     * @param updatedDetails new details
     * @return updated ItemReport
     * @throws ResourceNotFoundException if report is not found
     * @throws BusinessRuleException     if attempting to update an already RETURNED report
     */
    @Transactional
    public ItemReport updateReport(Long id, ItemReport updatedDetails) {
        ItemReport existing = getReportById(id);

        if (existing.getStatus() == ItemStatus.RETURNED) {
            throw new BusinessRuleException("Cannot modify an item report that has already been marked as RETURNED.");
        }

        existing.setTitle(updatedDetails.getTitle());
        existing.setDescription(updatedDetails.getDescription());
        existing.setCategory(updatedDetails.getCategory());
        existing.setLocation(updatedDetails.getLocation());

        return itemReportRepository.save(existing);
    }

    /**
     * Marks an item report as RETURNED after the return process is complete.
     * Enforces that an item can only be marked as RETURNED from an appropriate prior state (CLAIMED).
     *
     * @param id report ID
     * @return updated ItemReport
     * @throws ResourceNotFoundException if report is not found
     * @throws BusinessRuleException     if item is not in CLAIMED status
     */
    @Transactional
    public ItemReport markAsReturned(Long id) {
        ItemReport report = getReportById(id);
        if (report.getStatus() != ItemStatus.CLAIMED) {
            throw new BusinessRuleException("An item report can only be marked as RETURNED from CLAIMED status. Current status: " + report.getStatus());
        }
        report.setStatus(ItemStatus.RETURNED);
        return itemReportRepository.save(report);
    }
}

