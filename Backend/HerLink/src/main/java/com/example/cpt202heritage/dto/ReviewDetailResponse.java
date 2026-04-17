package com.example.cpt202heritage.dto;

import java.util.List;

public record ReviewDetailResponse(
        Long submissionId,
        Long resourceId,
        Integer versionNo,
        ResourceReviewStatus resourceStatus,
        ResourceSection resource,
        ContributorSection contributor,
        CategorySection category,
        SubmissionSection submission,
        List<String> tags,
        List<ReviewHistoryItemResponse> reviewHistory
) {
}
