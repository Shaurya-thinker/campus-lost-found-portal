package com.campus.lostfound.dto;

/**
 * Report response representing the count of unclaimed found items in a specific item category.
 */
public class CategoryCountReportResponse {

    private String category;
    private long itemCount;

    public CategoryCountReportResponse() {
    }

    public CategoryCountReportResponse(String category, long itemCount) {
        this.category = category;
        this.itemCount = itemCount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public long getItemCount() {
        return itemCount;
    }

    public void setItemCount(long itemCount) {
        this.itemCount = itemCount;
    }
}

