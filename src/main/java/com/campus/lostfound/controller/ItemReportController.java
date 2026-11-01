package com.campus.lostfound.controller;

import com.campus.lostfound.dto.ItemReportCreateRequest;
import com.campus.lostfound.dto.ItemReportResponse;
import com.campus.lostfound.dto.ItemReportUpdateRequest;
import com.campus.lostfound.entity.ItemReport;
import com.campus.lostfound.entity.enums.ItemStatus;
import com.campus.lostfound.entity.enums.ReportType;
import com.campus.lostfound.service.ItemReportService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller managing lost and found item reports.
 * Base path: /api/reports
 */
@RestController
@RequestMapping("/api/reports")
public class ItemReportController {

    private final ItemReportService itemReportService;

    public ItemReportController(ItemReportService itemReportService) {
        this.itemReportService = itemReportService;
    }

    /**
     * Creates a new LOST or FOUND report.
     * POST /api/reports
     */
    @PostMapping
    public ResponseEntity<ItemReportResponse> createReport(@Valid @RequestBody ItemReportCreateRequest request) {
        ItemReport report = request.toEntity();
        ItemReport createdReport = itemReportService.createReport(report, request.getReporterId());
        return ResponseEntity.status(HttpStatus.CREATED).body(ItemReportResponse.fromEntity(createdReport));
    }

    /**
     * Retrieves all reports, optionally filtered by report type (LOST/FOUND) and/or status (REPORTED/CLAIMED/RETURNED).
     * GET /api/reports
     * GET /api/reports?type=FOUND&status=REPORTED
     */
    @GetMapping
    public ResponseEntity<List<ItemReportResponse>> getReports(
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) ItemStatus status) {
        List<ItemReport> reports = itemReportService.getReportsByTypeAndStatus(type, status);
        List<ItemReportResponse> response = reports.stream()
                .map(ItemReportResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves an individual item report by its ID.
     * GET /api/reports/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ItemReportResponse> getReportById(@PathVariable Long id) {
        ItemReport report = itemReportService.getReportById(id);
        return ResponseEntity.ok(ItemReportResponse.fromEntity(report));
    }

    /**
     * Searches reports using category and/or location keyword parameters.
     * GET /api/reports/search?category=Electronics&location=Library
     */
    @GetMapping("/search")
    public ResponseEntity<List<ItemReportResponse>> searchReports(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String location) {
        List<ItemReport> reports = itemReportService.searchReports(category, location);
        List<ItemReportResponse> response = reports.stream()
                .map(ItemReportResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Retrieves all reports submitted by a specific user.
     * GET /api/reports/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ItemReportResponse>> getReportsByUser(@PathVariable Long userId) {
        List<ItemReport> reports = itemReportService.getReportsByReporterId(userId);
        List<ItemReportResponse> response = reports.stream()
                .map(ItemReportResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(response);
    }

    /**
     * Updates permitted details of an existing item report.
     * PUT /api/reports/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ItemReportResponse> updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ItemReportUpdateRequest request) {
        ItemReport updated = itemReportService.updateReport(id, request.toEntity());
        return ResponseEntity.ok(ItemReportResponse.fromEntity(updated));
    }

    /**
     * Marks an item report as RETURNED after ownership verification and item handover.
     * PATCH /api/reports/{id}/returned
     */
    @PatchMapping("/{id}/returned")
    public ResponseEntity<ItemReportResponse> markAsReturned(@PathVariable Long id) {
        ItemReport returned = itemReportService.markAsReturned(id);
        return ResponseEntity.ok(ItemReportResponse.fromEntity(returned));
    }
}

