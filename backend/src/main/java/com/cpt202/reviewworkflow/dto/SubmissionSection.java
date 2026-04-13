package com.cpt202.reviewworkflow.dto;

import java.time.LocalDateTime;

public record SubmissionSection(
    LocalDateTime submittedAt,
    Long submittedBy,
    String submissionNote,
    ResourceReviewStatus statusSnapshot,
    boolean resubmission,
    String currentContextLabel
) {
}
