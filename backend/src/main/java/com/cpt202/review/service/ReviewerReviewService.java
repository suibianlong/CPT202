package com.cpt202.review.service;

import com.cpt202.review.dto.PageResponse;
import com.cpt202.review.dto.ReviewDecisionRequest;
import com.cpt202.review.dto.ReviewDecisionResponse;
import com.cpt202.review.dto.ReviewDetailResponse;
import com.cpt202.review.dto.ReviewListItemResponse;

public interface ReviewerReviewService {
    PageResponse<ReviewListItemResponse> getPendingReviews(int page, int pageSize);

    ReviewDetailResponse getReviewDetail(Long submissionId);

    ReviewDecisionResponse submitDecision(Long submissionId, ReviewDecisionRequest request);
}
