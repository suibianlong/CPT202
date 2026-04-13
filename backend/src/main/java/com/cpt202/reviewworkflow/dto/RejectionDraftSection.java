package com.cpt202.reviewworkflow.dto;

import java.time.LocalDateTime;

public record RejectionDraftSection(
    String feedbackComment,
    LocalDateTime savedAt,
    Long savedByReviewerId,
    String savedByReviewerName
) {
}
