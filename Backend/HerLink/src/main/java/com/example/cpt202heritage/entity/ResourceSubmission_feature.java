package com.example.cpt202heritage.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "resource_submission")
public class ResourceSubmission_feature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "submission_id")
    private Long submissionId;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo = 1;

    @ManyToOne
    @JoinColumn(name = "submitted_by", nullable = false)
    private User submittedBy;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "submission_note", columnDefinition = "TEXT")
    private String submissionNote;

    @Column(name = "status_snapshot", nullable = false)
    @Enumerated(EnumType.STRING)
    private StatusSnapshot statusSnapshot = StatusSnapshot.PENDING_REVIEW;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL)
    private Set<ReviewRecord> reviewRecords = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        submittedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    public enum StatusSnapshot {
        DRAFT, PENDING_REVIEW, REJECTED, APPROVED, ARCHIVED
    }
}
