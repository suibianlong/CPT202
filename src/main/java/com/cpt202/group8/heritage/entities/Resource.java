package com.cpt202.group8.heritage.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "resource")
public class Resource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "contributor_id", nullable = false)
    private Long contributorId;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @Column(name = "place", length = 255)
    private String place;

    @Column(name = "preview_image", length = 500)
    private String previewImage;

    @Column(name = "media_url", length = 500)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    private ResourceStatus status;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    public Resource() {
    }

    public Resource(Long contributorId, String title, String description, Long categoryId,
                    String place, String previewImage, String mediaUrl, ResourceStatus status,
                    LocalDateTime reviewedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
                    LocalDateTime archivedAt, String resourceType) {
        this.contributorId = contributorId;
        this.title = title;
        this.description = description;
        this.categoryId = categoryId;
        this.place = place;
        this.previewImage = previewImage;
        this.mediaUrl = mediaUrl;
        this.status = status;
        this.reviewedAt = reviewedAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.archivedAt = archivedAt;
        this.resourceType = resourceType;
    }

    public Long getId() {
        return id;
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

    public ResourceStatus getStatus() {
        return status;
    }

    public void setStatus(ResourceStatus status) {
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

    @Override
    public String toString() {
        return "Resource{" +
                "id=" + id +
                ", contributorId=" + contributorId +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", categoryId=" + categoryId +
                ", place='" + place + '\'' +
                ", previewImage='" + previewImage + '\'' +
                ", mediaUrl='" + mediaUrl + '\'' +
                ", status=" + status +
                ", reviewedAt=" + reviewedAt +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", archivedAt=" + archivedAt +
                ", resourceType='" + resourceType + '\'' +
                '}';
    }
}