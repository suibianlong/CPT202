export type ResourceReviewStatus =
  | "DRAFT"
  | "PENDING_REVIEW"
  | "APPROVED"
  | "REJECTED"
  | "ARCHIVED";

export type ReviewAction = "APPROVE" | "REJECT";
export type ReviewHistoryContextType = "CURRENT_SUBMISSION" | "PREVIOUS_SUBMISSION";

export interface PageResponse<T> {
  items: T[];
  page: number;
  pageSize: number;
  total: number;
}

export interface ReviewListItem {
  submissionId: number;
  resourceId: number;
  versionNo: number;
  title: string;
  contributorId: number;
  contributorName: string;
  categoryId: number;
  categoryType: string;
  categoryTopic: string;
  submittedAt: string;
  resourceStatus: Extract<ResourceReviewStatus, "PENDING_REVIEW">;
}

export interface ReviewHistoryItem {
  reviewRecordId: number;
  resourceId: number;
  submissionId: number;
  versionNo: number;
  reviewerId: number;
  reviewerName: string;
  action: ReviewAction;
  status: Extract<ResourceReviewStatus, "APPROVED" | "REJECTED">;
  feedbackComment?: string | null;
  reviewedAt: string;
  contextType: ReviewHistoryContextType;
  contextLabel: string;
}

export interface RejectionDraft {
  feedbackComment: string;
  savedAt: string;
  savedByReviewerId: number;
  savedByReviewerName: string;
}

export interface ReviewDetail {
  submissionId: number;
  resourceId: number;
  versionNo: number;
  resourceStatus: ResourceReviewStatus;
  resource: {
    title: string;
    description: string;
    place?: string | null;
    resourceType: string;
    previewImage?: string | null;
    mediaUrl?: string | null;
    copyrightDeclaration?: string | null;
    usageDeclaration?: string | null;
    visibleToUsers: boolean;
    createdAt: string;
    updatedAt: string;
    reviewedAt?: string | null;
  };
  contributor: {
    userId: number;
    username: string;
  };
  category: {
    categoryId: number;
    categoryType: string;
    categoryTopic: string;
  };
  submission: {
    submittedAt: string;
    submittedBy: number;
    submissionNote?: string | null;
    statusSnapshot: Extract<ResourceReviewStatus, "PENDING_REVIEW">;
    resubmission: boolean;
    currentContextLabel: string;
  };
  tags: string[];
  rejectionDraft?: RejectionDraft | null;
  reviewHistory: ReviewHistoryItem[];
}

export type ReviewDecisionPayload =
  | {
      submissionId: number;
      resourceId: number;
      versionNo: number;
      reviewerId: number;
      action: "APPROVE";
      feedbackComment?: string | null;
    }
  | {
      submissionId: number;
      resourceId: number;
      versionNo: number;
      reviewerId: number;
      action: "REJECT";
      feedbackComment: string;
    };

export interface ReviewDecisionResponse {
  reviewRecordId: number;
  submissionId: number;
  resourceId: number;
  versionNo: number;
  action: ReviewAction;
  resourceStatus: Extract<ResourceReviewStatus, "APPROVED" | "REJECTED">;
  feedbackComment?: string | null;
  reviewedAt: string;
  removedFromPendingQueue: boolean;
  visibleToUsers: boolean;
}

export interface ReviewDraftPayload {
  submissionId: number;
  reviewerId: number;
  feedbackComment: string;
}

export interface ReviewDraftResponse extends RejectionDraft {
  submissionId: number;
}
