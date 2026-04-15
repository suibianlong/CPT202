package org.example.cpt202music.model.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Entity
@Table(name = "review_records")
public class ReviewRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "resource_id", nullable = false)
    private Resource resource;

    @ManyToOne
    @JoinColumn(name = "submission_id", nullable = false)
    private ResourceSubmission submission;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo = 1;

    @ManyToOne
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Column(name = "action_description", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActionDescription actionDescription;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ReviewStatus status;

    @Column(name = "feedback_comment", columnDefinition = "TEXT")
    private String feedbackComment;

    @Column(name = "reviewed_at", nullable = false)
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "reviewRecord", cascade = CascadeType.ALL)
    private Set<Feedback> feedbacks = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        reviewedAt = LocalDateTime.now();
        createdAt = LocalDateTime.now();
    }

    public enum ActionDescription {
        APPROVE, REJECT
    }

    public enum ReviewStatus {
        APPROVED, REJECTED
    }
}
