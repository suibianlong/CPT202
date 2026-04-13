package com.cpt202.reviewworkflow.dto;

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
    RejectionDraftSection rejectionDraft,
    List<ReviewHistoryItemResponse> reviewHistory
) {
}
