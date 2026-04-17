package com.example.cpt202heritage.vo;

import java.io.Serializable;
import java.time.LocalDateTime;

// display brief information
public class ResourceListItemVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String previewImage;
    private String status;
    private String resourceType;
    private Long categoryId;
    private LocalDateTime updatedAt;

    public ResourceListItemVO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(String previewImage) {
        this.previewImage = previewImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "ResourceListItemVO{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", previewImage='" + previewImage + '\'' +
                ", status='" + status + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", categoryId=" + categoryId +
                ", updatedAt=" + updatedAt +
                '}';
    }
}