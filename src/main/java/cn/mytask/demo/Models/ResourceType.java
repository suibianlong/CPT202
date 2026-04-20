package cn.mytask.demo.Models;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "resourceType")
public class ResourceType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resourceTypeId")
    private Long resourceTypeId;

    @Column(name = "typeName")
    private String typeName;

    @Column(name = "status")
    private String status;

    @Column(name = "usageCount")
    private Integer usageCount;

    @Column(name = "createdAt")
    private LocalDateTime createdAt;

    @Column(name = "lastUpdatedAt")
    private LocalDateTime lastUpdatedAt;

    public ResourceType() {
    }

    public ResourceType(Long resourceTypeId, String typeName, String status,
                        Integer usageCount, LocalDateTime createdAt, LocalDateTime lastUpdatedAt) {
        this.resourceTypeId = resourceTypeId;
        this.typeName = typeName;
        this.status = status;
        this.usageCount = usageCount;
        this.createdAt = createdAt;
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public Long getResourceTypeId() {
        return resourceTypeId;
    }

    public void setResourceTypeId(Long resourceTypeId) {
        this.resourceTypeId = resourceTypeId;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }
}