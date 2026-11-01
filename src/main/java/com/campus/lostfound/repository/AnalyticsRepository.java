package com.campus.lostfound.repository;

import com.campus.lostfound.dto.CategoryCountReportResponse;
import com.campus.lostfound.dto.LocationCountReportResponse;
import com.campus.lostfound.dto.UnclaimedItemDetailResponse;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;

/**
 * Spring JDBC repository using JdbcTemplate for custom analytical SQL queries.
 */
@Repository
public class AnalyticsRepository {

    private final JdbcTemplate jdbcTemplate;

    public AnalyticsRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Report A: Unclaimed found items grouped by location.
     * Filters: report_type = 'FOUND' AND status = 'REPORTED'
     * Order: item_count DESC, location ASC
     */
    public List<LocationCountReportResponse> findUnclaimedItemCountsByLocation() {
        String sql = """
            SELECT location, COUNT(*) AS item_count
            FROM item_reports
            WHERE report_type = ? AND status = ?
            GROUP BY location
            ORDER BY item_count DESC, location ASC
            """;

        RowMapper<LocationCountReportResponse> rowMapper = (rs, rowNum) ->
                new LocationCountReportResponse(
                        rs.getString("location"),
                        rs.getLong("item_count")
                );

        return jdbcTemplate.query(sql, rowMapper, "FOUND", "REPORTED");
    }

    /**
     * Report B: Unclaimed found items grouped by category.
     * Filters: report_type = 'FOUND' AND status = 'REPORTED'
     * Order: item_count DESC, category ASC
     */
    public List<CategoryCountReportResponse> findUnclaimedItemCountsByCategory() {
        String sql = """
            SELECT category, COUNT(*) AS item_count
            FROM item_reports
            WHERE report_type = ? AND status = ?
            GROUP BY category
            ORDER BY item_count DESC, category ASC
            """;

        RowMapper<CategoryCountReportResponse> rowMapper = (rs, rowNum) ->
                new CategoryCountReportResponse(
                        rs.getString("category"),
                        rs.getLong("item_count")
                );

        return jdbcTemplate.query(sql, rowMapper, "FOUND", "REPORTED");
    }

    /**
     * Report C: Detailed list of unclaimed found items with reporter details.
     * Filters: ir.report_type = 'FOUND' AND ir.status = 'REPORTED'
     * Join: item_reports JOIN users ON ir.user_id = u.id
     * Order: ir.report_date DESC, ir.id DESC
     */
    public List<UnclaimedItemDetailResponse> findDetailedUnclaimedItems() {
        String sql = """
            SELECT ir.id AS report_id,
                   ir.title AS title,
                   ir.category AS category,
                   ir.location AS location,
                   ir.report_date AS report_date,
                   u.id AS reporter_id,
                   u.name AS reporter_name
            FROM item_reports ir
            JOIN users u ON ir.user_id = u.id
            WHERE ir.report_type = ? AND ir.status = ?
            ORDER BY ir.report_date DESC, ir.id DESC
            """;

        RowMapper<UnclaimedItemDetailResponse> rowMapper = (rs, rowNum) -> {
            Date sqlDate = rs.getDate("report_date");
            return new UnclaimedItemDetailResponse(
                    rs.getLong("report_id"),
                    rs.getString("title"),
                    rs.getString("category"),
                    rs.getString("location"),
                    sqlDate != null ? sqlDate.toLocalDate() : null,
                    rs.getLong("reporter_id"),
                    rs.getString("reporter_name")
            );
        };

        return jdbcTemplate.query(sql, rowMapper, "FOUND", "REPORTED");
    }
}

