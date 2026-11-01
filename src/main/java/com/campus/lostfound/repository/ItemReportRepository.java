package com.campus.lostfound.repository;

import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.enums.ItemStatus;
import com.campus.lostfound.entity.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for ItemReport entity operations.
 */
@Repository
public interface ItemReportRepository extends JpaRepository<ItemReport, Long> {

    /**
     * Finds all item reports matching a specific status (e.g., REPORTED, CLAIMED, RETURNED).
     *
     * @param status item lifecycle status
     * @return list of matching item reports
     */
    List<ItemReport> findByStatus(ItemStatus status);

    /**
     * Finds all item reports matching a specific report type (e.g., LOST, FOUND).
     *
     * @param reportType type of report
     * @return list of matching item reports
     */
    List<ItemReport> findByReportType(ReportType reportType);

    /**
     * Finds all item reports matching both report type and status.
     *
     * @param reportType type of report (LOST or FOUND)
     * @param status item lifecycle status
     * @return list of matching item reports
     */
    List<ItemReport> findByReportTypeAndStatus(ReportType reportType, ItemStatus status);

    /**
     * Finds all item reports matching a category, ignoring case.
     *
     * @param category category to search for
     * @return list of matching item reports
     */
    List<ItemReport> findByCategoryIgnoreCase(String category);

    /**
     * Finds all item reports matching a location, ignoring case.
     *
     * @param location location to search for
     * @return list of matching item reports
     */
    List<ItemReport> findByLocationIgnoreCase(String location);

    /**
     * Finds item reports matching both category and location, ignoring case.
     *
     * @param category category name
     * @param location location name
     * @return list of matching item reports
     */
    List<ItemReport> findByCategoryIgnoreCaseAndLocationIgnoreCase(String category, String location);

    /**
     * Finds all reports created by a specific user via their reporter ID.
     *
     * @param reporterId ID of the reporting user
     * @return list of item reports created by the user
     */
    List<ItemReport> findByReporterId(Long reporterId);
}

