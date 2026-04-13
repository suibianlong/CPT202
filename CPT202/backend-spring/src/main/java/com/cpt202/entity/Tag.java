package com.cpt202.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_id")
    private Long tagId;

    @Column(name = "tag_name", nullable = false, unique = true, length = 100)
    private String tagName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private TagStatus status = TagStatus.ACTIVE;

    @Column(name = "usage_count", nullable = false)
    private Integer usageCount = 0;

    @Column(name = "created_time", nullable = false)
    private LocalDateTime createdTime;

    @Column(name = "last_updated_time", nullable = false)
    private LocalDateTime lastUpdatedTime;

    @ManyToMany(mappedBy = "tags")
    private Set<Resource> resources = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdTime = LocalDateTime.now();
        lastUpdatedTime = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdatedTime = LocalDateTime.now();
    }

    public enum TagStatus {
        ACTIVE, INACTIVE
    }
}
