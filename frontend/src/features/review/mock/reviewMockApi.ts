import { initialPendingReviewItems, initialReviewDetails } from "./reviewMockData";
import {
  PageResponse,
  ReviewDecisionPayload,
  ReviewDecisionResponse,
  ReviewDetail,
  ReviewDraftPayload,
  ReviewDraftResponse,
  ReviewHistoryItem,
  ReviewListItem,
} from "../types";

let pendingItems = clone(initialPendingReviewItems);
let reviewDetails = clone(initialReviewDetails);
let nextReviewRecordId = 500;

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}

function delay(ms = 250) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms);
  });
}

export async function fetchPendingReviews(): Promise<PageResponse<ReviewListItem>> {
  await delay();

  const sortedItems = [...pendingItems].sort((left, right) =>
    left.submittedAt.localeCompare(right.submittedAt),
  );

  return {
    items: clone(sortedItems),
    page: 1,
    pageSize: 10,
    total: sortedItems.length,
  };
}

export async function fetchReviewDetail(submissionId: number): Promise<ReviewDetail> {
  await delay();

  const detail = reviewDetails[submissionId];
  if (!detail) {
    throw new Error(`Review detail for submission ${submissionId} was not found.`);
  }

  return clone(detail);
}

export async function submitReviewDecision(
  payload: ReviewDecisionPayload,
): Promise<ReviewDecisionResponse> {
  await delay();

  const detail = reviewDetails[payload.submissionId];
  if (!detail) {
    throw new Error(`Review detail for submission ${payload.submissionId} was not found.`);
  }

  if (detail.resourceStatus !== "PENDING_REVIEW") {
    throw new Error("This submission is no longer pending review.");
  }

  if (payload.action === "REJECT" && !payload.feedbackComment.trim()) {
    throw new Error("Rejection comments are required");
  }

  const reviewedAt = new Date().toISOString();
  const resourceStatus = payload.action === "APPROVE" ? "APPROVED" : "REJECTED";
  const feedbackComment =
    payload.action === "REJECT" ? payload.feedbackComment.trim() : payload.feedbackComment?.trim() ?? null;
  const visibleToUsers = payload.action === "APPROVE";

  const historyItem: ReviewHistoryItem = {
    reviewRecordId: nextReviewRecordId++,
    resourceId: detail.resourceId,
    submissionId: detail.submissionId,
    versionNo: detail.versionNo,
    reviewerId: payload.reviewerId,
    reviewerName: `reviewer_${payload.reviewerId}`,
    action: payload.action,
    status: resourceStatus,
    feedbackComment,
    reviewedAt,
    contextType: "CURRENT_SUBMISSION",
    contextLabel: detail.submission.currentContextLabel,
  };

  reviewDetails = {
    ...reviewDetails,
    [payload.submissionId]: {
      ...detail,
      resourceStatus,
      resource: {
        ...detail.resource,
        visibleToUsers,
        updatedAt: reviewedAt,
        reviewedAt,
      },
      rejectionDraft: null,
      reviewHistory: [...detail.reviewHistory, historyItem],
    },
  };

  pendingItems = pendingItems.filter((item) => item.submissionId !== payload.submissionId);

  return {
    reviewRecordId: historyItem.reviewRecordId,
    submissionId: detail.submissionId,
    resourceId: detail.resourceId,
    versionNo: detail.versionNo,
    action: payload.action,
    resourceStatus,
    feedbackComment,
    reviewedAt,
    removedFromPendingQueue: true,
    visibleToUsers,
  };
}

export async function saveRejectionDraft(
  payload: ReviewDraftPayload,
): Promise<ReviewDraftResponse> {
  await delay();

  const detail = reviewDetails[payload.submissionId];
  if (!detail) {
    throw new Error(`Review detail for submission ${payload.submissionId} was not found.`);
  }

  const feedbackComment = payload.feedbackComment.trim();
  if (!feedbackComment) {
    throw new Error("Rejection draft cannot be empty.");
  }

  const savedAt = new Date().toISOString();
  const savedByReviewerName = `reviewer_${payload.reviewerId}`;

  reviewDetails = {
    ...reviewDetails,
    [payload.submissionId]: {
      ...detail,
      rejectionDraft: {
        feedbackComment,
        savedAt,
        savedByReviewerId: payload.reviewerId,
        savedByReviewerName,
      },
    },
  };

  return {
    submissionId: payload.submissionId,
    feedbackComment,
    savedAt,
    savedByReviewerId: payload.reviewerId,
    savedByReviewerName,
  };
}
