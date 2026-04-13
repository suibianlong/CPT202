package com.cpt202.review.controller;

import com.cpt202.review.dto.PageResponse;
import com.cpt202.review.dto.ReviewDecisionRequest;
import com.cpt202.review.dto.ReviewDecisionResponse;
import com.cpt202.review.dto.ReviewDetailResponse;
import com.cpt202.review.dto.ReviewListItemResponse;
import com.cpt202.review.service.ReviewerReviewService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reviewer/reviews")
public class ReviewerReviewController {

    private final ReviewerReviewService reviewerReviewService;

    public ReviewerReviewController(ReviewerReviewService reviewerReviewService) {
        this.reviewerReviewService = reviewerReviewService;
    }

    @GetMapping("/pending")
    public PageResponse<ReviewListItemResponse> getPendingReviews(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int pageSize
    ) {
        return reviewerReviewService.getPendingReviews(page, pageSize);
    }

    @GetMapping("/submissions/{submissionId}")
    public ReviewDetailResponse getReviewDetail(@PathVariable Long submissionId) {
        return reviewerReviewService.getReviewDetail(submissionId);
    }

    @PostMapping("/submissions/{submissionId}/decision")
    public ReviewDecisionResponse submitDecision(
            @PathVariable Long submissionId,
            @Valid @RequestBody ReviewDecisionRequest request
    ) {
        return reviewerReviewService.submitDecision(submissionId, request);
    }
}
