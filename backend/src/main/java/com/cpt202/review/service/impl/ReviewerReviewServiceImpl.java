package com.cpt202.review.service.impl;

import com.cpt202.review.dto.CategorySection;
import com.cpt202.review.dto.ContributorSection;
import com.cpt202.review.dto.PageResponse;
import com.cpt202.review.dto.ResourceReviewStatus;
import com.cpt202.review.dto.ResourceSection;
import com.cpt202.review.dto.ReviewAction;
import com.cpt202.review.dto.ReviewDecisionRequest;
import com.cpt202.review.dto.ReviewDecisionResponse;
import com.cpt202.review.dto.ReviewDetailResponse;
import com.cpt202.review.dto.ReviewHistoryItemResponse;
import com.cpt202.review.dto.ReviewListItemResponse;
import com.cpt202.review.dto.SubmissionSection;
import com.cpt202.review.service.ReviewerReviewService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ReviewerReviewServiceImpl implements ReviewerReviewService {
    // Replace this in-memory store with repository queries once the real entities are ready.

    private final Map<Long, ReviewDetailResponse> submissionStore = new ConcurrentHashMap<>();
    private final AtomicLong reviewRecordIdSequence;

    public ReviewerReviewServiceImpl() {
        List<ReviewDetailResponse> seedData = buildSeedData();
        seedData.forEach(detail -> submissionStore.put(detail.submissionId(), detail));

        long maxReviewRecordId = seedData.stream()
                .flatMap(detail -> detail.reviewHistory().stream())
                .mapToLong(ReviewHistoryItemResponse::reviewRecordId)
                .max()
                .orElse(100L);

        this.reviewRecordIdSequence = new AtomicLong(maxReviewRecordId + 1);
    }

    @Override
    public PageResponse<ReviewListItemResponse> getPendingReviews(int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safePageSize = Math.min(Math.max(pageSize, 1), 50);

        List<ReviewListItemResponse> pendingItems = submissionStore.values().stream()
                .filter(detail -> detail.resourceStatus() == ResourceReviewStatus.PENDING_REVIEW)
                .sorted(Comparator.comparing(detail -> detail.submission().submittedAt()))
                .map(this::toListItem)
                .toList();

        int fromIndex = Math.min((safePage - 1) * safePageSize, pendingItems.size());
        int toIndex = Math.min(fromIndex + safePageSize, pendingItems.size());

        return new PageResponse<>(
                pendingItems.subList(fromIndex, toIndex),
                safePage,
                safePageSize,
                pendingItems.size()
        );
    }

    @Override
    public ReviewDetailResponse getReviewDetail(Long submissionId) {
        ReviewDetailResponse detail = submissionStore.get(submissionId);
        if (detail == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Submission " + submissionId + " does not exist."
            );
        }
        return detail;
    }

    @Override
    public ReviewDecisionResponse submitDecision(Long submissionId, ReviewDecisionRequest request) {
        ReviewDetailResponse currentDetail = getReviewDetail(submissionId);

        if (!submissionId.equals(request.submissionId())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Path submissionId must match request body submissionId."
            );
        }

        if (currentDetail.resourceStatus() != ResourceReviewStatus.PENDING_REVIEW) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "This submission is no longer pending review."
            );
        }

        LocalDateTime now = LocalDateTime.now();
        ResourceReviewStatus nextStatus = request.action() == ReviewAction.APPROVE
                ? ResourceReviewStatus.APPROVED
                : ResourceReviewStatus.REJECTED;

        ReviewHistoryItemResponse newHistoryItem = new ReviewHistoryItemResponse(
                reviewRecordIdSequence.getAndIncrement(),
                currentDetail.resourceId(),
                currentDetail.submissionId(),
                currentDetail.versionNo(),
                request.reviewerId(),
                "reviewer_" + request.reviewerId(),
                request.action(),
                nextStatus,
                request.feedbackComment(),
                now
        );

        ReviewDetailResponse updatedDetail = updateDetailAfterDecision(currentDetail, nextStatus, now, newHistoryItem);
        submissionStore.put(submissionId, updatedDetail);

        return new ReviewDecisionResponse(
                newHistoryItem.reviewRecordId(),
                updatedDetail.submissionId(),
                updatedDetail.resourceId(),
                updatedDetail.versionNo(),
                request.action(),
                nextStatus,
                request.feedbackComment(),
                now,
                true
        );
    }

    private ReviewListItemResponse toListItem(ReviewDetailResponse detail) {
        return new ReviewListItemResponse(
                detail.submissionId(),
                detail.resourceId(),
                detail.versionNo(),
                detail.resource().title(),
                detail.contributor().userId(),
                detail.contributor().username(),
                detail.category().categoryId(),
                detail.category().categoryType(),
                detail.category().categoryTopic(),
                detail.submission().submittedAt(),
                detail.resourceStatus()
        );
    }

    private ReviewDetailResponse updateDetailAfterDecision(
            ReviewDetailResponse currentDetail,
            ResourceReviewStatus nextStatus,
            LocalDateTime reviewedAt,
            ReviewHistoryItemResponse newHistoryItem
    ) {
        List<ReviewHistoryItemResponse> updatedHistory = new ArrayList<>();
        updatedHistory.add(newHistoryItem);
        updatedHistory.addAll(currentDetail.reviewHistory());

        ResourceSection updatedResource = new ResourceSection(
                currentDetail.resource().title(),
                currentDetail.resource().description(),
                currentDetail.resource().place(),
                currentDetail.resource().resourceType(),
                currentDetail.resource().previewImage(),
                currentDetail.resource().mediaUrl(),
                currentDetail.resource().createdAt(),
                reviewedAt,
                reviewedAt
        );

        return new ReviewDetailResponse(
                currentDetail.submissionId(),
                currentDetail.resourceId(),
                currentDetail.versionNo(),
                nextStatus,
                updatedResource,
                currentDetail.contributor(),
                currentDetail.category(),
                currentDetail.submission(),
                currentDetail.tags(),
                List.copyOf(updatedHistory)
        );
    }

    private List<ReviewDetailResponse> buildSeedData() {
        return List.of(
                buildStudySpaceGuide(),
                buildCareerWorkshopChecklist()
        );
    }

    private ReviewDetailResponse buildStudySpaceGuide() {
        return new ReviewDetailResponse(
                301L,
                1001L,
                2,
                ResourceReviewStatus.PENDING_REVIEW,
                new ResourceSection(
                        "Campus Study Space Guide",
                        "A guide to quiet study areas on campus with updated link information.",
                        "Library Building A",
                        "LINK",
                        "https://cdn.example.com/resource/1001/preview.png",
                        "https://example.com/study-spaces",
                        LocalDateTime.parse("2026-03-20T10:00:00"),
                        LocalDateTime.parse("2026-04-08T09:30:00"),
                        null
                ),
                new ContributorSection(18L, "alice"),
                new CategorySection(7L, "Places", "Study Space"),
                new SubmissionSection(
                        LocalDateTime.parse("2026-04-08T09:30:00"),
                        18L,
                        "Updated broken link and preview image.",
                        ResourceReviewStatus.PENDING_REVIEW
                ),
                List.of("campus", "study"),
                List.of(
                        new ReviewHistoryItemResponse(
                                200L,
                                1001L,
                                288L,
                                1,
                                2L,
                                "reviewer_anna",
                                ReviewAction.REJECT,
                                ResourceReviewStatus.REJECTED,
                                "Please replace the broken media link.",
                                LocalDateTime.parse("2026-03-21T14:00:00")
                        )
                )
        );
    }

    private ReviewDetailResponse buildCareerWorkshopChecklist() {
        return new ReviewDetailResponse(
                302L,
                1002L,
                1,
                ResourceReviewStatus.PENDING_REVIEW,
                new ResourceSection(
                        "Career Workshop Preparation Checklist",
                        "Checklist for students attending the career workshop for the first time.",
                        "Student Centre",
                        "PDF",
                        "https://cdn.example.com/resource/1002/preview.png",
                        "https://cdn.example.com/resource/1002/checklist.pdf",
                        LocalDateTime.parse("2026-04-04T15:10:00"),
                        LocalDateTime.parse("2026-04-07T18:20:00"),
                        null
                ),
                new ContributorSection(26L, "ben"),
                new CategorySection(8L, "Events", "Workshop"),
                new SubmissionSection(
                        LocalDateTime.parse("2026-04-07T18:20:00"),
                        26L,
                        "Initial submission for reviewer approval.",
                        ResourceReviewStatus.PENDING_REVIEW
                ),
                List.of("career", "checklist", "event"),
                List.of()
        );
    }
}
