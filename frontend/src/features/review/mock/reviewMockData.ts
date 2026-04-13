import { ReviewDetail, ReviewListItem } from "../types";

export const initialPendingReviewItems: ReviewListItem[] = [
  {
    submissionId: 301,
    resourceId: 1001,
    versionNo: 2,
    title: "Campus Study Space Guide",
    contributorId: 18,
    contributorName: "alice",
    categoryId: 7,
    categoryType: "Places",
    categoryTopic: "Study Space",
    submittedAt: "2026-04-08T09:30:00",
    resourceStatus: "PENDING_REVIEW",
  },
  {
    submissionId: 302,
    resourceId: 1002,
    versionNo: 1,
    title: "Chemistry Lab Safety Checklist",
    contributorId: 22,
    contributorName: "ben",
    categoryId: 9,
    categoryType: "Objects",
    categoryTopic: "Safety",
    submittedAt: "2026-04-08T08:45:00",
    resourceStatus: "PENDING_REVIEW",
  },
];

export const initialReviewDetails: Record<number, ReviewDetail> = {
  301: {
    submissionId: 301,
    resourceId: 1001,
    versionNo: 2,
    resourceStatus: "PENDING_REVIEW",
    resource: {
      title: "Campus Study Space Guide",
      description: "A guide to the best quiet areas on campus for students who need focused study time.",
      place: "Library Building A",
      resourceType: "LINK",
      previewImage: "https://cdn.example.com/resource/1001/preview.png",
      mediaUrl: "https://example.com/study-spaces",
      copyrightDeclaration:
        "Contributor declares the preview image is self-created and the linked content is attributed.",
      usageDeclaration:
        "May be published to all campus users after approval for non-commercial educational use.",
      visibleToUsers: false,
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
      submissionNote: "Updated link, preview image, and corrected source attribution.",
      statusSnapshot: "PENDING_REVIEW",
      resubmission: true,
      currentContextLabel: "Current resubmission · Version 2 fixing issues from the prior rejection",
    },
    tags: ["campus", "study", "quiet"],
    rejectionDraft: null,
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
        feedbackComment: "Please replace the broken media link and add a usage declaration.",
        reviewedAt: "2026-03-21T14:00:00",
        contextType: "PREVIOUS_SUBMISSION",
        contextLabel: "Previous submission · Version 1 first review",
      },
    ],
  },
  302: {
    submissionId: 302,
    resourceId: 1002,
    versionNo: 1,
    resourceStatus: "PENDING_REVIEW",
    resource: {
      title: "Chemistry Lab Safety Checklist",
      description: "Checklist and onboarding notes for entering the chemistry lab safely.",
      place: "Science Building B",
      resourceType: "PDF",
      previewImage: "https://cdn.example.com/resource/1002/checklist-cover.png",
      mediaUrl: "https://example.com/lab-safety-checklist.pdf",
      copyrightDeclaration:
        "Submitted PDF is based on department-owned material with permission from the lab manager.",
      usageDeclaration:
        "May be published to students enrolled in laboratory modules after reviewer approval.",
      visibleToUsers: false,
      createdAt: "2026-04-01T11:15:00",
      updatedAt: "2026-04-08T08:45:00",
      reviewedAt: null,
    },
    contributor: {
      userId: 22,
      username: "ben",
    },
    category: {
      categoryId: 9,
      categoryType: "Objects",
      categoryTopic: "Safety",
    },
    submission: {
      submittedAt: "2026-04-08T08:45:00",
      submittedBy: 22,
      submissionNote: "Initial submission for reviewer approval.",
      statusSnapshot: "PENDING_REVIEW",
      resubmission: false,
      currentContextLabel: "Initial submission · Version 1 awaiting first reviewer decision",
    },
    tags: ["lab", "safety", "checklist"],
    rejectionDraft: {
      feedbackComment:
        "Please confirm whether the departmental usage declaration covers off-campus sharing.",
      savedAt: "2026-04-08T10:20:00",
      savedByReviewerId: 2,
      savedByReviewerName: "reviewer_2",
    },
    reviewHistory: [],
  },
};
