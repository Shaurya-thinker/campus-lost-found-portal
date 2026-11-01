package com.campus.lostfound.dto;

import java.time.LocalDate;

/**
 * Report response providing detailed row-level attributes for unclaimed found items.
 */
public class UnclaimedItemDetailResponse {

    private Long reportId;
    private String title;
    private String category;
    private String location;
    private LocalDate reportDate;
    private Long reporterId;
    private String reporterName;

    public UnclaimedItemDetailResponse() {
    }

    public UnclaimedItemDetailResponse(Long reportId, String title, String category,
                                       String location, LocalDate reportDate,
                                       Long reporterId, String reporterName) {
        this.reportId = reportId;
        this.title = title;
        this.category = category;
        this.location = location;
        this.reportDate = reportDate;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
    }

    public Long getReportId() {
        return reportId;
    }

    public void setReportId(Long reportId) {
        this.reportId = reportId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public LocalDate getReportDate() {
        return reportDate;
    }

    public void setReportDate(LocalDate reportDate) {
        this.reportDate = reportDate;
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

