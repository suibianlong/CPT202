package com.cpt202.reviewworkflow.controller;

import com.cpt202.reviewworkflow.dto.PageResponse;
import com.cpt202.reviewworkflow.dto.ReviewDecisionRequest;
import com.cpt202.reviewworkflow.dto.ReviewDecisionResponse;
import com.cpt202.reviewworkflow.dto.ReviewDetailResponse;
import com.cpt202.reviewworkflow.dto.ReviewDraftRequest;
import com.cpt202.reviewworkflow.dto.ReviewDraftResponse;
import com.cpt202.reviewworkflow.dto.ReviewListItemResponse;
import com.cpt202.reviewworkflow.service.ReviewService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

@Validated
@RestController
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:5173"})
@RequestMapping("/api/reviewer/reviews")
public class ReviewController {
    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/pending")
    public PageResponse<ReviewListItemResponse> getPendingReviews(
        @RequestParam(defaultValue = "1") @Min(1) int page,
        @RequestParam(defaultValue = "10") @Min(1) @Max(50) int pageSize
    ) {
        return reviewService.getPendingReviews(page, pageSize);
    }

    @GetMapping("/submissions/{submissionId}")
    public ReviewDetailResponse getReviewDetail(@PathVariable Long submissionId) {
        return reviewService.getReviewDetail(submissionId);
    }

    @PostMapping("/submissions/{submissionId}/decision")
    public ReviewDecisionResponse submitDecision(
        @PathVariable Long submissionId,
        @Valid @RequestBody ReviewDecisionRequest request
    ) {
        return reviewService.submitDecision(submissionId, request);
    }

    @PutMapping("/submissions/{submissionId}/rejection-draft")
    public ReviewDraftResponse saveRejectionDraft(
        @PathVariable Long submissionId,
        @Valid @RequestBody ReviewDraftRequest request
    ) {
        return reviewService.saveRejectionDraft(submissionId, request);
    }
}
