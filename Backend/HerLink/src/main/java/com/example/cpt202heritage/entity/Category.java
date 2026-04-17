package com.example.cpt202heritage.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

// corresponding to the "category" table
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long categoryId;
    private String categoryType;
    private String categoryTopic;
    private String status;
    private Integer usageCount;
    private LocalDateTime createdTime;
    private LocalDateTime lastUpdatedTime;

    public Category() {
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryType() {
        return categoryType;
    }

    public void setCategoryType(String categoryType) {
        this.categoryType = categoryType;
    }

    public String getCategoryTopic() {
        return categoryTopic;
    }

    public void setCategoryTopic(String categoryTopic) {
        this.categoryTopic = categoryTopic;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getUsageCount() {
        return usageCount;
    }

    public void setUsageCount(Integer usageCount) {
        this.usageCount = usageCount;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }

    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(LocalDateTime lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId=" + categoryId +
                ", categoryType='" + categoryType + '\'' +
                ", categoryTopic='" + categoryTopic + '\'' +
                ", status='" + status + '\'' +
                ", usageCount=" + usageCount +
                ", createdTime=" + createdTime +
                ", lastUpdatedTime=" + lastUpdatedTime +
                '}';
    }
}
