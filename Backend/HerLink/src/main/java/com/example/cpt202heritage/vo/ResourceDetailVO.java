package com.example.cpt202heritage.vo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.List;

// show to users
public class ResourceDetailVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private Long contributorId;
    private String title;
    private String description;
    private Long categoryId;
    private String place;
    private String previewImage;
    private String mediaUrl;
    private String status;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime archivedAt;
    private String resourceType;
    private List<Long> tagIds;

    public ResourceDetailVO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContributorId() {
        return contributorId;
    }

    public void setContributorId(Long contributorId) {
        this.contributorId = contributorId;
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

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public String getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(String previewImage) {
        this.previewImage = previewImage;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(LocalDateTime reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public List<Long> getTagIds() {
        return tagIds;
    }

    public void setTagIds(List<Long> tagIds) {
        this.tagIds = tagIds;
    }

    @Override
    public String toString() {
        return "ResourceDetailVO{" +
            "id=" + id +
            ", contributorId=" + contributorId +
            ", title='" + title + '\'' +
            ", description='" + description + '\'' +
            ", categoryId=" + categoryId +
            ", place='" + place + '\'' +
            ", previewImage='" + previewImage + '\'' +
            ", mediaUrl='" + mediaUrl + '\'' +
            ", status='" + status + '\'' +
            ", reviewedAt=" + reviewedAt +
            ", createdAt=" + createdAt +
            ", updatedAt=" + updatedAt +
            ", archivedAt=" + archivedAt +
            ", resourceType='" + resourceType + '\'' +
            ", tagIds=" + tagIds +
            '}';
}
}