package com.campus.lostfound.dto;

/**
 * Report response representing the count of unclaimed found items at a specific campus location.
 */
public class LocationCountReportResponse {

    private String location;
    private long itemCount;

    public LocationCountReportResponse() {
    }

    public LocationCountReportResponse(String location, long itemCount) {
        this.location = location;
        this.itemCount = itemCount;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public long getItemCount() {
        return itemCount;
    }

    public void setItemCount(long itemCount) {
        this.itemCount = itemCount;
    }
}

