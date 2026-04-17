package com.example.cpt202heritage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "resources")
public class Resource_feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "resource_id")
    private Long resourceId;

    @ManyToOne
    @JoinColumn(name = "contributor_id", nullable = false)
    private User contributor;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(length = 255)
    private String place;

    @Column(name = "preview_image", length = 500)
    private String previewImage;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ResourceType type;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ResourceStatus status = ResourceStatus.DRAFT;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "archived_at")
    private LocalDateTime archivedAt;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "last_updated_time", nullable = false)
    private LocalDateTime lastUpdatedTime;

    @Column(name = "last_submitted_time")
    private LocalDateTime lastSubmittedTime;

    @Column(name = "last_published_time")
    private LocalDateTime lastPublishedTime;

    @ManyToMany
    @JoinTable(
        name = "resource_category",
        joinColumns = @JoinColumn(name = "resource_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "resource_tag",
        joinColumns = @JoinColumn(name = "resource_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    private Set<AttachedFile> attachedFiles = new HashSet<>();

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    private Set<ResourceSubmission> submissions = new HashSet<>();

    @OneToMany(mappedBy = "resource", cascade = CascadeType.ALL)
    private Set<ReviewRecord> reviewRecords = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        lastUpdatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedTime = LocalDateTime.now();
    }

    public enum ResourceType {
        VIDEO, IMAGE, DOCUMENT, AUDIO
    }

    public enum ResourceStatus {
        DRAFT, PENDING_REVIEW, REJECTED, APPROVED, ARCHIVED
    }
}
