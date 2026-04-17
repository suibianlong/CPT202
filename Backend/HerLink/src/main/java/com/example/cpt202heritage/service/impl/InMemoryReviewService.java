package com.example.cpt202heritage.service.impl;

import com.example.cpt202heritage.dto.CategorySection;
import com.example.cpt202heritage.dto.ContributorSection;
import com.example.cpt202heritage.dto.PageResponse;
import com.example.cpt202heritage.dto.RejectionDraftSection;
import com.example.cpt202heritage.dto.ResourceReviewStatus;
import com.example.cpt202heritage.dto.ResourceSection;
import com.example.cpt202heritage.dto.ReviewAction;
import com.example.cpt202heritage.dto.ReviewDecisionRequest;
import com.example.cpt202heritage.dto.ReviewDecisionResponse;
import com.example.cpt202heritage.dto.ReviewDetailResponse;
import com.example.cpt202heritage.dto.ReviewDraftRequest;
import com.example.cpt202heritage.dto.ReviewDraftResponse;
import com.example.cpt202heritage.dto.ReviewHistoryContextType;
import com.example.cpt202heritage.dto.ReviewHistoryItemResponse;
import com.example.cpt202heritage.dto.ReviewListItemResponse;
import com.example.cpt202heritage.dto.SubmissionSection;
import com.example.cpt202heritage.exception.ReviewNotFoundException;
import com.example.cpt202heritage.exception.ReviewStatusConflictException;
import com.example.cpt202heritage.exception.ReviewValidationException;
import com.example.cpt202heritage.service.ReviewService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Service;

@Service
public class InMemoryReviewService implements ReviewService {
    private static final Long DEFAULT_REVIEWER_ID = 2L;

    private final Map<Long, ReviewAggregate> submissions = new ConcurrentHashMap<>();
    private final AtomicLong reviewRecordIdSequence = new AtomicLong(300);

    public InMemoryReviewService() {
        seedData();
    }

    @Override
    public PageResponse<ReviewListItemResponse> getPendingReviews(int page, int pageSize) {
        validatePagination(page, pageSize);

        List<ReviewListItemResponse> pendingItems = submissions.values().stream()
            .filter(aggregate -> aggregate.resourceStatus == ResourceReviewStatus.PENDING_REVIEW)
            .sorted(Comparator.comparing(aggregate -> aggregate.submittedAt))
            .map(this::toReviewListItem)
            .toList();

        int fromIndex = Math.min((page - 1) * pageSize, pendingItems.size());
        int toIndex = Math.min(fromIndex + pageSize, pendingItems.size());

        return new PageResponse<>(
            pendingItems.subList(fromIndex, toIndex),
            page,
            pageSize,
            pendingItems.size()
        );
    }

    @Override
    public ReviewDetailResponse getReviewDetail(Long submissionId) {
        return toReviewDetail(getSubmission(submissionId));
    }

    @Override
    public synchronized ReviewDecisionResponse submitDecision(Long submissionId, ReviewDecisionRequest request) {
        validateSubmissionIdConsistency(submissionId, request.submissionId());

        ReviewAggregate aggregate = getSubmission(submissionId);
        validateDecisionRequest(aggregate, request);

        LocalDateTime reviewedAt = LocalDateTime.now();
        ResourceReviewStatus nextStatus = request.action() == ReviewAction.APPROVE
            ? ResourceReviewStatus.APPROVED
            : ResourceReviewStatus.REJECTED;
        String feedbackComment = normalizeComment(request.feedbackComment());
        boolean visibleToUsers = request.action() == ReviewAction.APPROVE;
        long reviewRecordId = reviewRecordIdSequence.incrementAndGet();

        aggregate.resourceStatus = nextStatus;
        aggregate.visibleToUsers = visibleToUsers;
        aggregate.reviewedAt = reviewedAt;
        aggregate.updatedAt = reviewedAt;
        clearDraft(aggregate);
        aggregate.reviewHistory.add(new ReviewHistoryModel(
            reviewRecordId,
            aggregate.resourceId,
            aggregate.submissionId,
            aggregate.versionNo,
            request.reviewerId(),
            reviewerName(request.reviewerId()),
            request.action(),
            nextStatus,
            feedbackComment,
            reviewedAt,
            ReviewHistoryContextType.CURRENT_SUBMISSION,
            aggregate.currentContextLabel
        ));

        return new ReviewDecisionResponse(
            reviewRecordId, 
            aggregate.submissionId, 
            aggregate.resourceId, 
            aggregate.versionNo, 
            request.action(), 
            nextStatus, 
            feedbackComment, 
            reviewedAt, 
            true, 
            visibleToUsers);
    }

    @Override
    public synchronized ReviewDraftResponse saveRejectionDraft(Long submissionId, ReviewDraftRequest request) {
        validateSubmissionIdConsistency(submissionId, request.submissionId());

        ReviewAggregate aggregate = getSubmission(submissionId);
        validatePendingStatus(aggregate);

        String feedbackComment = normalizeComment(request.feedbackComment());
        if (feedbackComment == null) {
            throw new ReviewValidationException(
                "Rejection draft cannot be empty.",
                Map.of("feedbackComment", "Rejection draft cannot be empty.")
            );
        }

        LocalDateTime savedAt = LocalDateTime.now();
        aggregate.rejectionDraftComment = feedbackComment;
        aggregate.rejectionDraftSavedAt = savedAt;
        aggregate.rejectionDraftReviewerId = request.reviewerId();
        aggregate.rejectionDraftReviewerName = reviewerName(request.reviewerId());

        return new ReviewDraftResponse(
            aggregate.submissionId,
            feedbackComment,
            savedAt,
            request.reviewerId(),
            aggregate.rejectionDraftReviewerName
        );
    }

    private void validatePagination(int page, int pageSize) {
        Map<String, String> details = new LinkedHashMap<>();

        if (page < 1) {
            details.put("page", "page must be at least 1");
        }
        if (pageSize < 1 || pageSize > 50) {
            details.put("pageSize", "pageSize must be between 1 and 50");
        }

        if (!details.isEmpty()) {
            throw new ReviewValidationException("Invalid pagination request.", details);
        }
    }

    private void validateDecisionRequest(ReviewAggregate aggregate, ReviewDecisionRequest request) {
        Map<String, String> details = new LinkedHashMap<>();

        if (!Objects.equals(request.resourceId(), aggregate.resourceId)) {
            details.put("resourceId", "resourceId does not match the selected submission.");
        }
        if (!Objects.equals(request.versionNo(), aggregate.versionNo)) {
            details.put("versionNo", "versionNo does not match the selected submission.");
        }
        if (request.action() == ReviewAction.REJECT && normalizeComment(request.feedbackComment()) == null) {
            details.put("feedbackComment", "Rejection comments are required");
        }

        if (!details.isEmpty()) {
            throw new ReviewValidationException("Review decision request is invalid.", details);
        }

        validatePendingStatus(aggregate);
    }

    private void validatePendingStatus(ReviewAggregate aggregate) {
        if (aggregate.resourceStatus != ResourceReviewStatus.PENDING_REVIEW) {
            throw new ReviewStatusConflictException("This submission is no longer pending review.");
        }
    }

    private void validateSubmissionIdConsistency(Long pathSubmissionId, Long bodySubmissionId) {
        if (!Objects.equals(pathSubmissionId, bodySubmissionId)) {
            throw new ReviewValidationException(
                "Submission ID in path and body must match.",
                Map.of(
                    "pathSubmissionId", String.valueOf(pathSubmissionId),
                    "bodySubmissionId", String.valueOf(bodySubmissionId)
                )
            );
        }
    }

    private String normalizeComment(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private String reviewerName(Long reviewerId) {
        return "reviewer_" + reviewerId;
    }

    private void clearDraft(ReviewAggregate aggregate) {
        aggregate.rejectionDraftComment = null;
        aggregate.rejectionDraftSavedAt = null;
        aggregate.rejectionDraftReviewerId = null;
        aggregate.rejectionDraftReviewerName = null;
    }

    private ReviewAggregate getSubmission(Long submissionId) {
        ReviewAggregate aggregate = submissions.get(submissionId);
        if (aggregate == null) {
            throw new ReviewNotFoundException(submissionId);
        }
        return aggregate;
    }

    private ReviewListItemResponse toReviewListItem(ReviewAggregate aggregate) {
        return new ReviewListItemResponse(
            aggregate.submissionId,
            aggregate.resourceId,
            aggregate.versionNo,
            aggregate.title,
            aggregate.contributorId,
            aggregate.contributorName,
            aggregate.categoryId,
            aggregate.categoryType,
            aggregate.categoryTopic,
            aggregate.submittedAt,
            aggregate.resourceStatus
        );
    }

    private ReviewDetailResponse toReviewDetail(ReviewAggregate aggregate) {
        List<ReviewHistoryItemResponse> history = aggregate.reviewHistory.stream()
            .sorted(Comparator.comparing(ReviewHistoryModel::reviewedAt))
            .map(item -> new ReviewHistoryItemResponse(
                item.reviewRecordId(),
                item.resourceId(),
                item.submissionId(),
                item.versionNo(),
                item.reviewerId(),
                item.reviewerName(),
                item.action(),
                item.status(),
                item.feedbackComment(),
                item.reviewedAt(),
                item.contextType(),
                item.contextLabel()
            ))
            .toList();

        RejectionDraftSection draftSection = aggregate.rejectionDraftComment == null
            ? null
            : new RejectionDraftSection(
                aggregate.rejectionDraftComment,
                aggregate.rejectionDraftSavedAt,
                aggregate.rejectionDraftReviewerId,
                aggregate.rejectionDraftReviewerName
            );

        return new ReviewDetailResponse(
            aggregate.submissionId,
            aggregate.resourceId,
            aggregate.versionNo,
            aggregate.resourceStatus,
            new ResourceSection(
                aggregate.title,
                aggregate.description,
                aggregate.place,
                aggregate.resourceType,
                aggregate.previewImage,
                aggregate.mediaUrl,
                aggregate.copyrightDeclaration,
                aggregate.usageDeclaration,
                aggregate.visibleToUsers,
                aggregate.createdAt,
                aggregate.updatedAt,
                aggregate.reviewedAt
            ),
            new ContributorSection(aggregate.contributorId, aggregate.contributorName),
            new CategorySection(aggregate.categoryId, aggregate.categoryType, aggregate.categoryTopic),
            new SubmissionSection(
                aggregate.submittedAt,
                aggregate.submittedBy,
                aggregate.submissionNote,
                aggregate.statusSnapshot,
                aggregate.resubmission,
                aggregate.currentContextLabel
            ),
            List.copyOf(aggregate.tags),
            draftSection,
            history
        );
    }

    private void seedData() {
        ReviewAggregate studySpace = new ReviewAggregate();
        studySpace.submissionId = 301L;
        studySpace.resourceId = 1001L;
        studySpace.versionNo = 2;
        studySpace.title = "Campus Study Space Guide";
        studySpace.description = "A reviewer-facing sample resource for quiet study locations.";
        studySpace.place = "Library Building A";
        studySpace.resourceType = "LINK";
        studySpace.previewImage = "https://cdn.example.com/resource/1001/preview.png";
        studySpace.mediaUrl = "https://example.com/study-spaces";
        studySpace.copyrightDeclaration =
            "Contributor declares the preview image is self-created and the linked content is attributed.";
        studySpace.usageDeclaration =
            "May be published to all campus users after approval for non-commercial educational use.";
        studySpace.visibleToUsers = false;
        studySpace.resourceStatus = ResourceReviewStatus.PENDING_REVIEW;
        studySpace.createdAt = LocalDateTime.of(2026, 3, 20, 10, 0);
        studySpace.updatedAt = LocalDateTime.of(2026, 4, 8, 9, 30);
        studySpace.reviewedAt = null;
        studySpace.categoryId = 7L;
        studySpace.categoryType = "Places";
        studySpace.categoryTopic = "Study Space";
        studySpace.contributorId = 18L;
        studySpace.contributorName = "alice";
        studySpace.submittedBy = 18L;
        studySpace.submittedAt = LocalDateTime.of(2026, 4, 8, 9, 30);
        studySpace.submissionNote = "Updated link, preview image, and corrected source attribution.";
        studySpace.statusSnapshot = ResourceReviewStatus.PENDING_REVIEW;
        studySpace.resubmission = true;
        studySpace.currentContextLabel = "Current resubmission · Version 2 fixing issues from the prior rejection";
        studySpace.tags.add("campus");
        studySpace.tags.add("study");
        studySpace.tags.add("quiet");
        studySpace.reviewHistory.add(new ReviewHistoryModel(
            200L,
            1001L,
            288L,
            1,
            DEFAULT_REVIEWER_ID,
            "reviewer_anna",
            ReviewAction.REJECT,
            ResourceReviewStatus.REJECTED,
            "Please replace the broken media link and add a usage declaration.",
            LocalDateTime.of(2026, 3, 21, 14, 0),
            ReviewHistoryContextType.PREVIOUS_SUBMISSION,
            "Previous submission · Version 1 first review"
        ));

        ReviewAggregate labSafety = new ReviewAggregate();
        labSafety.submissionId = 302L;
        labSafety.resourceId = 1002L;
        labSafety.versionNo = 1;
        labSafety.title = "Chemistry Lab Safety Checklist";
        labSafety.description = "Checklist and onboarding notes for entering the chemistry lab.";
        labSafety.place = "Science Building B";
        labSafety.resourceType = "PDF";
        labSafety.previewImage = "https://cdn.example.com/resource/1002/checklist-cover.png";
        labSafety.mediaUrl = "https://example.com/lab-safety-checklist.pdf";
        labSafety.copyrightDeclaration =
            "Submitted PDF is based on department-owned material with permission from the lab manager.";
        labSafety.usageDeclaration =
            "May be published to students enrolled in laboratory modules after reviewer approval.";
        labSafety.visibleToUsers = false;
        labSafety.resourceStatus = ResourceReviewStatus.PENDING_REVIEW;
        labSafety.createdAt = LocalDateTime.of(2026, 4, 1, 11, 15);
        labSafety.updatedAt = LocalDateTime.of(2026, 4, 8, 8, 45);
        labSafety.reviewedAt = null;
        labSafety.categoryId = 9L;
        labSafety.categoryType = "Objects";
        labSafety.categoryTopic = "Safety";
        labSafety.contributorId = 22L;
        labSafety.contributorName = "ben";
        labSafety.submittedBy = 22L;
        labSafety.submittedAt = LocalDateTime.of(2026, 4, 8, 8, 45);
        labSafety.submissionNote = "Initial submission for reviewer approval.";
        labSafety.statusSnapshot = ResourceReviewStatus.PENDING_REVIEW;
        labSafety.resubmission = false;
        labSafety.currentContextLabel = "Initial submission · Version 1 awaiting first reviewer decision";
        labSafety.tags.add("lab");
        labSafety.tags.add("safety");
        labSafety.tags.add("checklist");
        labSafety.rejectionDraftComment =
            "Please confirm whether the departmental usage declaration covers off-campus sharing.";
        labSafety.rejectionDraftSavedAt = LocalDateTime.of(2026, 4, 8, 10, 20);
        labSafety.rejectionDraftReviewerId = DEFAULT_REVIEWER_ID;
        labSafety.rejectionDraftReviewerName = reviewerName(DEFAULT_REVIEWER_ID);

        ReviewAggregate busGuide = new ReviewAggregate();
        busGuide.submissionId = 303L;
        busGuide.resourceId = 1003L;
        busGuide.versionNo = 1;
        busGuide.title = "Campus Shuttle Route Guide";
        busGuide.description =
            "A quick guide to shuttle stop locations, service hours, and recommended transfer points for new students.";
        busGuide.place = "Main Gate Transit Hub";
        busGuide.resourceType = "LINK";
        busGuide.previewImage = "https://cdn.example.com/resource/1003/shuttle-guide.png";
        busGuide.mediaUrl = "https://example.com/campus-shuttle-guide";
        busGuide.copyrightDeclaration =
            "Map annotations were created by the contributor based on publicly posted campus transport information.";
        busGuide.usageDeclaration =
            "May be shared with all platform users as a student support resource after approval.";
        busGuide.visibleToUsers = false;
        busGuide.resourceStatus = ResourceReviewStatus.PENDING_REVIEW;
        busGuide.createdAt = LocalDateTime.of(2026, 4, 2, 9, 0);
        busGuide.updatedAt = LocalDateTime.of(2026, 4, 8, 9, 55);
        busGuide.reviewedAt = null;
        busGuide.categoryId = 11L;
        busGuide.categoryType = "Places";
        busGuide.categoryTopic = "Transport";
        busGuide.contributorId = 31L;
        busGuide.contributorName = "carol";
        busGuide.submittedBy = 31L;
        busGuide.submittedAt = LocalDateTime.of(2026, 4, 8, 9, 55);
        busGuide.submissionNote = "Initial route guide with updated stop names for the spring semester.";
        busGuide.statusSnapshot = ResourceReviewStatus.PENDING_REVIEW;
        busGuide.resubmission = false;
        busGuide.currentContextLabel = "Initial submission · Version 1 waiting for route and rights review";
        busGuide.tags.add("transport");
        busGuide.tags.add("shuttle");
        busGuide.tags.add("new-students");

        ReviewAggregate writingWorkshop = new ReviewAggregate();
        writingWorkshop.submissionId = 304L;
        writingWorkshop.resourceId = 1004L;
        writingWorkshop.versionNo = 3;
        writingWorkshop.title = "Academic Writing Workshop Replay";
        writingWorkshop.description =
            "Recorded workshop covering citation basics, paragraph structure, and common writing mistakes for first-year students.";
        writingWorkshop.place = "Online Resource Centre";
        writingWorkshop.resourceType = "VIDEO";
        writingWorkshop.previewImage = "https://cdn.example.com/resource/1004/writing-workshop-cover.png";
        writingWorkshop.mediaUrl = "https://example.com/writing-workshop-replay";
        writingWorkshop.copyrightDeclaration =
            "Contributor confirms speaker consent was obtained and the slides used in the recording are institution-owned.";
        writingWorkshop.usageDeclaration =
            "May be released to enrolled students for academic support and internal learning purposes.";
        writingWorkshop.visibleToUsers = false;
        writingWorkshop.resourceStatus = ResourceReviewStatus.PENDING_REVIEW;
        writingWorkshop.createdAt = LocalDateTime.of(2026, 3, 15, 15, 40);
        writingWorkshop.updatedAt = LocalDateTime.of(2026, 4, 8, 10, 35);
        writingWorkshop.reviewedAt = null;
        writingWorkshop.categoryId = 13L;
        writingWorkshop.categoryType = "Objects";
        writingWorkshop.categoryTopic = "Academic Skills";
        writingWorkshop.contributorId = 28L;
        writingWorkshop.contributorName = "david";
        writingWorkshop.submittedBy = 28L;
        writingWorkshop.submittedAt = LocalDateTime.of(2026, 4, 8, 10, 35);
        writingWorkshop.submissionNote = "Re-uploaded the replay with trimmed intro and clearer subtitle track.";
        writingWorkshop.statusSnapshot = ResourceReviewStatus.PENDING_REVIEW;
        writingWorkshop.resubmission = true;
        writingWorkshop.currentContextLabel =
            "Current resubmission · Version 3 revised after subtitle accuracy feedback";
        writingWorkshop.tags.add("writing");
        writingWorkshop.tags.add("workshop");
        writingWorkshop.tags.add("video");
        writingWorkshop.reviewHistory.add(new ReviewHistoryModel(
            214L,
            1004L,
            299L,
            2,
            DEFAULT_REVIEWER_ID,
            "reviewer_anna",
            ReviewAction.REJECT,
            ResourceReviewStatus.REJECTED,
            "Please fix subtitle timing and remove the unlicensed intro music before resubmission.",
            LocalDateTime.of(2026, 4, 4, 16, 20),
            ReviewHistoryContextType.PREVIOUS_SUBMISSION,
            "Previous submission · Version 2 rejected for media compliance issues"
        ));

        ReviewAggregate internshipFaq = new ReviewAggregate();
        internshipFaq.submissionId = 305L;
        internshipFaq.resourceId = 1005L;
        internshipFaq.versionNo = 1;
        internshipFaq.title = "Internship Application FAQ";
        internshipFaq.description =
            "An FAQ page answering common internship application questions, timelines, and document preparation tips.";
        internshipFaq.place = "Careers Portal";
        internshipFaq.resourceType = "PDF";
        internshipFaq.previewImage = "https://cdn.example.com/resource/1005/internship-faq-cover.png";
        internshipFaq.mediaUrl = "https://example.com/internship-application-faq.pdf";
        internshipFaq.copyrightDeclaration =
            "Contributor compiled the content with permission from the Careers Office based on official guidance.";
        internshipFaq.usageDeclaration =
            "May be shown to all authenticated users as a general employability support resource.";
        internshipFaq.visibleToUsers = false;
        internshipFaq.resourceStatus = ResourceReviewStatus.PENDING_REVIEW;
        internshipFaq.createdAt = LocalDateTime.of(2026, 4, 5, 13, 10);
        internshipFaq.updatedAt = LocalDateTime.of(2026, 4, 8, 11, 5);
        internshipFaq.reviewedAt = null;
        internshipFaq.categoryId = 15L;
        internshipFaq.categoryType = "Objects";
        internshipFaq.categoryTopic = "Careers";
        internshipFaq.contributorId = 35L;
        internshipFaq.contributorName = "emma";
        internshipFaq.submittedBy = 35L;
        internshipFaq.submittedAt = LocalDateTime.of(2026, 4, 8, 11, 5);
        internshipFaq.submissionNote = "Initial FAQ pack prepared for the careers support section.";
        internshipFaq.statusSnapshot = ResourceReviewStatus.PENDING_REVIEW;
        internshipFaq.resubmission = false;
        internshipFaq.currentContextLabel = "Initial submission · Version 1 pending content review";
        internshipFaq.tags.add("careers");
        internshipFaq.tags.add("internship");
        internshipFaq.tags.add("faq");

        submissions.put(studySpace.submissionId, studySpace);
        submissions.put(labSafety.submissionId, labSafety);
        submissions.put(busGuide.submissionId, busGuide);
        submissions.put(writingWorkshop.submissionId, writingWorkshop);
        submissions.put(internshipFaq.submissionId, internshipFaq);
    }

    private record ReviewHistoryModel(
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

    private static final class ReviewAggregate {
        private Long submissionId;
        private Long resourceId;
        private Integer versionNo;
        private String title;
        private String description;
        private String place;
        private String resourceType;
        private String previewImage;
        private String mediaUrl;
        private String copyrightDeclaration;
        private String usageDeclaration;
        private boolean visibleToUsers;
        private ResourceReviewStatus resourceStatus;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime reviewedAt;
        private Long categoryId;
        private String categoryType;
        private String categoryTopic;
        private Long contributorId;
        private String contributorName;
        private Long submittedBy;
        private LocalDateTime submittedAt;
        private String submissionNote;
        private ResourceReviewStatus statusSnapshot;
        private boolean resubmission;
        private String currentContextLabel;
        private String rejectionDraftComment;
        private LocalDateTime rejectionDraftSavedAt;
        private Long rejectionDraftReviewerId;
        private String rejectionDraftReviewerName;
        private final List<String> tags = new ArrayList<>();
        private final List<ReviewHistoryModel> reviewHistory = new ArrayList<>();
    }
}
