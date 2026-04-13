package com.cpt202.review.dto;

import java.time.LocalDateTime;

public record ReviewListItemResponse(
        Long submissionId,
        Long resourceId,
        Integer versionNo,
        String title,
        Long contributorId,
        String contributorName,
        Long categoryId,
        String categoryType,
        String categoryTopic,
        LocalDateTime submittedAt,
        ResourceReviewStatus resourceStatus
) {
}
