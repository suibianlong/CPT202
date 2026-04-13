package com.cpt202.reviewworkflow.service;

import com.cpt202.reviewworkflow.dto.PageResponse;
import com.cpt202.reviewworkflow.dto.ReviewDecisionRequest;
import com.cpt202.reviewworkflow.dto.ReviewDecisionResponse;
import com.cpt202.reviewworkflow.dto.ReviewDetailResponse;
import com.cpt202.reviewworkflow.dto.ReviewDraftRequest;
import com.cpt202.reviewworkflow.dto.ReviewDraftResponse;
import com.cpt202.reviewworkflow.dto.ReviewListItemResponse;

public interface ReviewService {

    PageResponse<ReviewListItemResponse> getPendingReviews(int page, int pageSize);

    ReviewDetailResponse getReviewDetail(Long submissionId);

    ReviewDecisionResponse submitDecision(Long submissionId, ReviewDecisionRequest request);

    ReviewDraftResponse saveRejectionDraft(Long submissionId, ReviewDraftRequest request);
}
