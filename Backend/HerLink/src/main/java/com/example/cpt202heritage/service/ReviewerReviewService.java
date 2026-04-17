package com.example.cpt202heritage.service;

import com.example.cpt202heritage.dto.PageResponse;
import com.example.cpt202heritage.dto.ReviewDecisionRequest;
import com.example.cpt202heritage.dto.ReviewDecisionResponse;
import com.example.cpt202heritage.dto.ReviewDetailResponse;
import com.example.cpt202heritage.dto.ReviewListItemResponse;

public interface ReviewerReviewService {
    PageResponse<ReviewListItemResponse> getPendingReviews(int page, int pageSize);

    ReviewDetailResponse getReviewDetail(Long submissionId);

    ReviewDecisionResponse submitDecision(Long submissionId, ReviewDecisionRequest request);
}
