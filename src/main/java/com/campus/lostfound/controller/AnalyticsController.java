package com.campus.lostfound.controller;

import com.campus.lostfound.dto.CategoryCountReportResponse;
import com.campus.lostfound.dto.LocationCountReportResponse;
import com.campus.lostfound.dto.UnclaimedItemDetailResponse;
import com.campus.lostfound.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * REST controller serving analytical reports and summaries generated via Spring JDBC.
 * Restricted strictly to campus administrators.
 * Base path: /api/analytics
 */
@RestController
@RequestMapping("/api/analytics")
@PreAuthorize("hasRole('ADMIN')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * Report A: Unclaimed found items grouped by location.
     * GET /api/analytics/unclaimed-items/by-location
     */
    @GetMapping("/unclaimed-items/by-location")
    public ResponseEntity<List<LocationCountReportResponse>> getUnclaimedItemsByLocation() {
        List<LocationCountReportResponse> report = analyticsService.getUnclaimedItemCountsByLocation();
        return ResponseEntity.ok(report);
    }

    /**
     * Report B: Unclaimed found items grouped by category.
     * GET /api/analytics/unclaimed-items/by-category
     */
    @GetMapping("/unclaimed-items/by-category")
    public ResponseEntity<List<CategoryCountReportResponse>> getUnclaimedItemsByCategory() {
        List<CategoryCountReportResponse> report = analyticsService.getUnclaimedItemCountsByCategory();
        return ResponseEntity.ok(report);
    }

    /**
     * Report C: Detailed list of unclaimed found items with reporter details.
     * GET /api/analytics/unclaimed-items/details
     */
    @GetMapping("/unclaimed-items/details")
    public ResponseEntity<List<UnclaimedItemDetailResponse>> getDetailedUnclaimedItems() {
        List<UnclaimedItemDetailResponse> report = analyticsService.getDetailedUnclaimedItems();
        return ResponseEntity.ok(report);
    }
}

