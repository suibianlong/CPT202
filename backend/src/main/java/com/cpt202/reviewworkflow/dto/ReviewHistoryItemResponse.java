package com.cpt202.reviewworkflow.dto;

import java.time.LocalDateTime;

public record ReviewHistoryItemResponse(
    Long reviewRecordId,
    Long resourceId,
    Long submissionId,
    Integer versionNo,
    Long reviewerId,
    String reviewerName,
    ReviewAction action,
    ResourceReviewStatus status,
    String feedbackComment,
    LocalDateTime reviewedAt,
    ReviewHistoryContextType contextType,
    String contextLabel
) {
}
