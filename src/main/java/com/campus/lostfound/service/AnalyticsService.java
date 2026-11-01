package com.campus.lostfound.service;

import com.campus.lostfound.dto.CategoryCountReportResponse;
import com.campus.lostfound.dto.LocationCountReportResponse;
import com.campus.lostfound.dto.UnclaimedItemDetailResponse;
import com.campus.lostfound.repository.AnalyticsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service orchestrating analytical reports and custom JDBC queries.
 */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final AnalyticsRepository analyticsRepository;

    public AnalyticsService(AnalyticsRepository analyticsRepository) {
        this.analyticsRepository = analyticsRepository;
    }

    /**
     * Generates report of unclaimed found items grouped by location.
     *
     * @return list of location counts sorted descending
     */
    public List<LocationCountReportResponse> getUnclaimedItemCountsByLocation() {
        return analyticsRepository.findUnclaimedItemCountsByLocation();
    }

    /**
     * Generates report of unclaimed found items grouped by category.
     *
     * @return list of category counts sorted descending
     */
    public List<CategoryCountReportResponse> getUnclaimedItemCountsByCategory() {
        return analyticsRepository.findUnclaimedItemCountsByCategory();
    }

    /**
     * Generates detailed list of all unclaimed found items joined with reporter data.
     *
     * @return list of unclaimed item details sorted newest first
     */
    public List<UnclaimedItemDetailResponse> getDetailedUnclaimedItems() {
        return analyticsRepository.findDetailedUnclaimedItems();
    }
}

