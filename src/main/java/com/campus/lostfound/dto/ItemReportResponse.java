package com.campus.lostfound.dto;

import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.enums.ItemStatus;
import com.campus.lostfound.entity.enums.ReportType;

import java.time.LocalDate;

/**
 * Response payload representing item report details without exposing entity internals.
 */
public class ItemReportResponse {

    private Long id;
    private String title;
    private String description;
    private String category;
    private String location;
    private ReportType reportType;
    private LocalDate reportDate;
    private ItemStatus status;
    private Long reporterId;
    private String reporterName;

    public ItemReportResponse() {
    }

    public ItemReportResponse(Long id, String title, String description, String category,
                              String location, ReportType reportType, LocalDate reportDate,
                              ItemStatus status, Long reporterId, String reporterName) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.reportType = reportType;
        this.reportDate = reportDate;
        this.status = status;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
    }

    public static ItemReportResponse fromEntity(ItemReport report) {
        if (report == null) {
            return null;
        }
        return new ItemReportResponse(
                report.getId(),
                report.getTitle(),
                report.getDescription(),
                report.getCategory(),
                report.getLocation(),
                report.getReportType(),
                report.getReportDate(),
                report.getStatus(),
                report.getReporter() != null ? report.getReporter().getId() : null,
                report.getReporter() != null ? report.getReporter().getName() : null
        );
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }
}

