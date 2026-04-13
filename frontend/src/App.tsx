import { useCallback, useEffect, useState } from "react";
import { ReviewDetailPage } from "./features/review/pages/ReviewDetailPage";
import { ReviewListPage } from "./features/review/pages/ReviewListPage";
import {
  fetchPendingReviews,
  fetchReviewDetail,
  saveRejectionDraft,
  submitReviewDecision,
} from "./features/review/api/reviewApi";
import {
  PageResponse,
  ReviewDecisionPayload,
  ReviewDetail,
  ReviewDraftPayload,
  ReviewListItem,
} from "./features/review/types";

type NoticeTone = "success" | "info";

interface NoticeState {
  message: string;
  tone: NoticeTone;
}

function App() {
  const [listResponse, setListResponse] = useState<PageResponse<ReviewListItem> | null>(null);
  const [selectedSubmissionId, setSelectedSubmissionId] = useState<number | null>(null);
  const [detail, setDetail] = useState<ReviewDetail | null>(null);
  const [listLoading, setListLoading] = useState(false);
  const [detailLoading, setDetailLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [savingDraft, setSavingDraft] = useState(false);
  const [listError, setListError] = useState<string | null>(null);
  const [detailError, setDetailError] = useState<string | null>(null);
  const [notice, setNotice] = useState<NoticeState | null>(null);

  const loadPendingReviews = useCallback(async (preferredSubmissionId?: number | null) => {
    setListLoading(true);
    setListError(null);

    try {
      const response = await fetchPendingReviews(1, 10);
      setListResponse(response);

      const nextSelection = response.items.find(
        (item) => item.submissionId === preferredSubmissionId,
      )
        ? preferredSubmissionId ?? null
        : response.items[0]?.submissionId ?? null;

      setSelectedSubmissionId(nextSelection);
    } catch (error) {
      setListError(error instanceof Error ? error.message : "Failed to load pending reviews.");
    } finally {
      setListLoading(false);
    }
  }, []);

  const loadReviewDetail = useCallback(async (submissionId: number) => {
    setDetailLoading(true);
    setDetailError(null);

    try {
      const response = await fetchReviewDetail(submissionId);
      setDetail(response);
    } catch (error) {
      setDetail(null);
      setDetailError(error instanceof Error ? error.message : "Failed to load review detail.");
    } finally {
      setDetailLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadPendingReviews();
  }, [loadPendingReviews]);

  useEffect(() => {
    if (selectedSubmissionId === null) {
      setDetail(null);
      return;
    }

    void loadReviewDetail(selectedSubmissionId);
  }, [loadReviewDetail, selectedSubmissionId]);

  useEffect(() => {
    if (!notice) {
      return undefined;
    }

    const timeoutId = window.setTimeout(() => {
      setNotice(null);
    }, 4800);

    return () => {
      window.clearTimeout(timeoutId);
    };
  }, [notice]);

  const handleDecision = useCallback(
    async (payload: ReviewDecisionPayload) => {
      setSubmitting(true);
      setNotice(null);
      setDetailError(null);

      try {
        const response = await submitReviewDecision(payload);
        setNotice({
          tone: "success",
          message:
            response.action === "APPROVE"
              ? "Submission approved, removed from the pending queue, and marked visible to general users."
              : "Submission rejected, comments recorded, and removed from the pending queue.",
        });
        await loadPendingReviews();
      } catch (error) {
        setDetailError(error instanceof Error ? error.message : "Failed to submit decision.");
      } finally {
        setSubmitting(false);
      }
    },
    [loadPendingReviews],
  );

  const handleSaveDraft = useCallback(
    async (payload: ReviewDraftPayload) => {
      setSavingDraft(true);
      setNotice(null);
      setDetailError(null);

      try {
        await saveRejectionDraft(payload);
        setNotice({
          tone: "info",
          message:
            "Rejection draft saved. You can return to the submission and continue editing at any time.",
        });
        await loadReviewDetail(payload.submissionId);
      } catch (error) {
        setDetailError(error instanceof Error ? error.message : "Failed to save rejection draft.");
      } finally {
        setSavingDraft(false);
      }
    },
    [loadReviewDetail],
  );

  const handleApprove = useCallback(() => {
    if (!detail) {
      return Promise.resolve();
    }

    return handleDecision({
      submissionId: detail.submissionId,
      resourceId: detail.resourceId,
      versionNo: detail.versionNo,
      reviewerId: 2,
      action: "APPROVE",
    });
  }, [detail, handleDecision]);

  const handleReject = useCallback(
    (feedbackComment: string) => {
      if (!detail) {
        return Promise.resolve();
      }

      return handleDecision({
        submissionId: detail.submissionId,
        resourceId: detail.resourceId,
        versionNo: detail.versionNo,
        reviewerId: 2,
        action: "REJECT",
        feedbackComment,
      });
    },
    [detail, handleDecision],
  );

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="header-content">
          <div className="header-copy">
            <p className="eyebrow">CPT202 Resource Platform</p>
            <h1>Review & Approval Workspace</h1>
            <p className="app-subtitle">
              Review pending submissions, inspect full resource detail, make publish decisions,
              and keep an auditable approval history in one place.
            </p>
          </div>
          <div className="hero-stat-grid">
            <div className="hero-stat-card">
              <span className="hero-stat-label">Pending Queue</span>
              <strong>{listResponse?.total ?? 0}</strong>
            </div>
            <div className="hero-stat-card">
              <span className="hero-stat-label">Active Submission</span>
              <strong>{selectedSubmissionId ?? "None"}</strong>
            </div>
            <div className="hero-stat-card">
              <span className="hero-stat-label">Current Version</span>
              <strong>{detail ? `v${detail.versionNo}` : "Waiting"}</strong>
            </div>
          </div>
        </div>
      </header>

      {notice ? (
        <div className={`notice-banner ${notice.tone}`} role="status" aria-live="polite">
          <span>{notice.message}</span>
          <button
            className="icon-button"
            type="button"
            aria-label="Dismiss notification"
            onClick={() => setNotice(null)}
          >
            Dismiss
          </button>
        </div>
      ) : null}

      <main className="app-layout">
        <ReviewListPage
          listResponse={listResponse}
          loading={listLoading}
          error={listError}
          selectedSubmissionId={selectedSubmissionId}
          onSelectSubmission={setSelectedSubmissionId}
        />
        <ReviewDetailPage
          detail={detail}
          loading={detailLoading}
          error={detailError}
          submitting={submitting}
          savingDraft={savingDraft}
          onApprove={handleApprove}
          onReject={handleReject}
          onSaveDraft={handleSaveDraft}
        />
      </main>
    </div>
  );
}

export default App;
