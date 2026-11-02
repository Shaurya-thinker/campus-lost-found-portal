package com.campus.lostfound.dto;

import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.enums.ReportType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request payload for creating a new lost or found item report.
 */
public class ItemReportCreateRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title must not exceed 150 characters")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    /**
     * Required for FOUND reports to assist in admin claim verification.
     * Optional / unused for LOST reports.
     */
    @Size(max = 1000, message = "Private description must not exceed 1000 characters")
    private String privateDescription;

    @NotBlank(message = "Category is required")
    @Size(max = 50, message = "Category must not exceed 50 characters")
    private String category;

    @NotBlank(message = "Location is required")
    @Size(max = 100, message = "Location must not exceed 100 characters")
    private String location;

    @NotNull(message = "Report type is required (LOST or FOUND)")
    private ReportType reportType;

    // Optional: Ignored and derived server-side from the authenticated principal
    private Long reporterId;

    public ItemReportCreateRequest() {
    }

    public ItemReportCreateRequest(String title, String description, String category,
                                   String location, ReportType reportType) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.reportType = reportType;
    }

    public ItemReportCreateRequest(String title, String description, String privateDescription,
                                   String category, String location, ReportType reportType) {
        this.title = title;
        this.description = description;
        this.privateDescription = privateDescription;
        this.category = category;
        this.location = location;
        this.reportType = reportType;
    }

    public ItemReportCreateRequest(String title, String description, String category,
                                   String location, ReportType reportType, Long reporterId) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.location = location;
        this.reportType = reportType;
        this.reporterId = reporterId;
    }

    public ItemReport toEntity() {
        ItemReport report = new ItemReport();
        report.setTitle(this.title);
        report.setDescription(this.description);
        report.setPrivateDescription(this.privateDescription);
        report.setCategory(this.category);
        report.setLocation(this.location);
        report.setReportType(this.reportType);
        return report;
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

    public String getPrivateDescription() {
        return privateDescription;
    }

    public void setPrivateDescription(String privateDescription) {
        this.privateDescription = privateDescription;
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

    public Long getReporterId() {
        return reporterId;
    }

    public void setReporterId(Long reporterId) {
        this.reporterId = reporterId;
    }
}
