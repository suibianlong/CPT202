import type {
  PageResponse,
  ResourceReviewStatus,
  ReviewDecisionPayload,
  ReviewDecisionResponse,
  ReviewDetail,
  ReviewHistoryItem,
  ReviewListItem,
} from "./types";

const baseReviewDetails: Record<number, ReviewDetail> = {
  301: {
    submissionId: 301,
    resourceId: 1001,
    versionNo: 2,
    resourceStatus: "PENDING_REVIEW",
    resource: {
      title: "Campus Study Space Guide",
      description:
        "A guide to quiet study areas on campus with updated link information.",
      place: "Library Building A",
      resourceType: "LINK",
      previewImage: "https://cdn.example.com/resource/1001/preview.png",
      mediaUrl: "https://example.com/study-spaces",
      createdAt: "2026-03-20T10:00:00",
      updatedAt: "2026-04-08T09:30:00",
      reviewedAt: null,
    },
    contributor: {
      userId: 18,
      username: "alice",
    },
    category: {
      categoryId: 7,
      categoryType: "Places",
      categoryTopic: "Study Space",
    },
    submission: {
      submittedAt: "2026-04-08T09:30:00",
      submittedBy: 18,
      submissionNote: "Updated broken link and preview image.",
      statusSnapshot: "PENDING_REVIEW",
    },
    tags: ["campus", "study"],
    reviewHistory: [
      {
        reviewRecordId: 200,
        resourceId: 1001,
        submissionId: 288,
        versionNo: 1,
        reviewerId: 2,
        reviewerName: "reviewer_anna",
        action: "REJECT",
        status: "REJECTED",
        feedbackComment: "Please replace the broken media link.",
        reviewedAt: "2026-03-21T14:00:00",
      },
    ],
  },
  302: {
    submissionId: 302,
    resourceId: 1002,
    versionNo: 1,
    resourceStatus: "PENDING_REVIEW",
    resource: {
      title: "Career Workshop Preparation Checklist",
      description:
        "Checklist for students attending the career workshop for the first time.",
      place: "Student Centre",
      resourceType: "PDF",
      previewImage: "https://cdn.example.com/resource/1002/preview.png",
      mediaUrl: "https://cdn.example.com/resource/1002/checklist.pdf",
      createdAt: "2026-04-04T15:10:00",
      updatedAt: "2026-04-07T18:20:00",
      reviewedAt: null,
    },
    contributor: {
      userId: 26,
      username: "ben",
    },
    category: {
      categoryId: 8,
      categoryType: "Events",
      categoryTopic: "Workshop",
    },
    submission: {
      submittedAt: "2026-04-07T18:20:00",
      submittedBy: 26,
      submissionNote: "Initial submission for reviewer approval.",
      statusSnapshot: "PENDING_REVIEW",
    },
    tags: ["career", "checklist", "event"],
    reviewHistory: [],
  },
};

let reviewRecordSequence = 300;
let reviewStore = createInitialStore();

export async function fetchPendingReviewsMock(
  page = 1,
  pageSize = 10,
): Promise<PageResponse<ReviewListItem>> {
  const safePage = Math.max(page, 1);
  const safePageSize = Math.min(Math.max(pageSize, 1), 50);
  const pendingItems = Array.from(reviewStore.values())
    .filter((detail) => detail.resourceStatus === "PENDING_REVIEW")
    .sort(
      (left, right) =>
        new Date(left.submission.submittedAt).getTime() -
        new Date(right.submission.submittedAt).getTime(),
    )
    .map(toListItem);

  const fromIndex = Math.min((safePage - 1) * safePageSize, pendingItems.length);
  const toIndex = Math.min(fromIndex + safePageSize, pendingItems.length);

  return {
    items: pendingItems.slice(fromIndex, toIndex),
    page: safePage,
    pageSize: safePageSize,
    total: pendingItems.length,
  };
}

export async function fetchReviewDetailMock(
  submissionId: number,
): Promise<ReviewDetail> {
  const detail = reviewStore.get(submissionId);
  if (!detail) {
    throw new Error(`Submission ${submissionId} does not exist.`);
  }
  return clone(detail);
}

export async function submitReviewDecisionMock(
  payload: ReviewDecisionPayload,
): Promise<ReviewDecisionResponse> {
  const detail = reviewStore.get(payload.submissionId);
  if (!detail) {
    throw new Error(`Submission ${payload.submissionId} does not exist.`);
  }

  if (detail.resourceStatus !== "PENDING_REVIEW") {
    throw new Error("This submission is no longer pending review.");
  }

  if (
    payload.action === "REJECT" &&
    (!payload.feedbackComment || !payload.feedbackComment.trim())
  ) {
    throw new Error("feedbackComment is required when action is REJECT.");
  }

  const reviewedAt = new Date().toISOString();
  const nextStatus: Extract<
    ResourceReviewStatus,
    "APPROVED" | "REJECTED"
  > = payload.action === "APPROVE" ? "APPROVED" : "REJECTED";

  const newHistoryItem: ReviewHistoryItem = {
    reviewRecordId: reviewRecordSequence++,
    resourceId: detail.resourceId,
    submissionId: detail.submissionId,
    versionNo: detail.versionNo,
    reviewerId: payload.reviewerId,
    reviewerName: `reviewer_${payload.reviewerId}`,
    action: payload.action,
    status: nextStatus,
    feedbackComment: payload.feedbackComment ?? null,
    reviewedAt,
  };

  const updatedDetail: ReviewDetail = {
    ...detail,
    resourceStatus: nextStatus,
    resource: {
      ...detail.resource,
      updatedAt: reviewedAt,
      reviewedAt,
    },
    reviewHistory: [newHistoryItem, ...detail.reviewHistory],
  };

  reviewStore.set(payload.submissionId, updatedDetail);

  return {
    reviewRecordId: newHistoryItem.reviewRecordId,
    submissionId: detail.submissionId,
    resourceId: detail.resourceId,
    versionNo: detail.versionNo,
    action: payload.action,
    resourceStatus: nextStatus,
    feedbackComment: payload.feedbackComment ?? null,
    reviewedAt,
    removedFromPendingQueue: true,
  };
}

export function resetReviewMockData(): void {
  reviewStore = createInitialStore();
  reviewRecordSequence = 300;
}

function createInitialStore(): Map<number, ReviewDetail> {
  return new Map(
    Object.values(baseReviewDetails).map((detail) => [detail.submissionId, clone(detail)]),
  );
}

function toListItem(detail: ReviewDetail): ReviewListItem {
  return {
    submissionId: detail.submissionId,
    resourceId: detail.resourceId,
    versionNo: detail.versionNo,
    title: detail.resource.title,
    contributorId: detail.contributor.userId,
    contributorName: detail.contributor.username,
    categoryId: detail.category.categoryId,
    categoryType: detail.category.categoryType,
    categoryTopic: detail.category.categoryTopic,
    submittedAt: detail.submission.submittedAt,
    resourceStatus: "PENDING_REVIEW",
  };
}

function clone<T>(value: T): T {
  return JSON.parse(JSON.stringify(value)) as T;
}
