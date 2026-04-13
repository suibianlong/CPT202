import { KeyboardEvent, useEffect, useMemo, useRef, useState } from "react";
import { RejectionDraft, ReviewDraftPayload } from "../types";

interface DecisionPanelProps {
  submitting: boolean;
  savingDraft: boolean;
  submissionId: number;
  reviewerId: number;
  initialDraft?: RejectionDraft | null;
  onApprove: () => Promise<void>;
  onReject: (feedbackComment: string) => Promise<void>;
  onSaveDraft: (payload: ReviewDraftPayload) => Promise<void>;
}

function formatDateTime(value: string) {
  return new Date(value).toLocaleString();
}

export function DecisionPanel({
  submitting,
  savingDraft,
  submissionId,
  reviewerId,
  initialDraft,
  onApprove,
  onReject,
  onSaveDraft,
}: DecisionPanelProps) {
  const [showRejectInput, setShowRejectInput] = useState(false);
  const [feedbackComment, setFeedbackComment] = useState("");
  const [validationError, setValidationError] = useState<string | null>(null);
  const textareaRef = useRef<HTMLTextAreaElement | null>(null);

  useEffect(() => {
    setFeedbackComment(initialDraft?.feedbackComment ?? "");
    setShowRejectInput(Boolean(initialDraft?.feedbackComment));
    setValidationError(null);
  }, [initialDraft?.feedbackComment, initialDraft?.savedAt]);

  useEffect(() => {
    if (!showRejectInput) {
      return;
    }

    textareaRef.current?.focus();
  }, [showRejectInput]);

  const trimmedFeedback = feedbackComment.trim();
  const initialDraftValue = initialDraft?.feedbackComment.trim() ?? "";

  const draftState = useMemo(() => {
    if (!trimmedFeedback.length) {
      return "empty";
    }

    if (!initialDraft) {
      return "dirty";
    }

    return trimmedFeedback === initialDraftValue ? "saved" : "dirty";
  }, [initialDraft, initialDraftValue, trimmedFeedback]);

  const handleApprove = async () => {
    setValidationError(null);
    const confirmed = window.confirm("Approve this submission and publish it to general users?");

    if (!confirmed) {
      return;
    }

    await onApprove();
  };

  const handleReject = async () => {
    if (!trimmedFeedback) {
      setValidationError("Rejection comments are required");
      return;
    }

    setValidationError(null);
    await onReject(trimmedFeedback);
    setFeedbackComment("");
    setShowRejectInput(false);
  };

  const handleSaveDraft = async () => {
    if (!trimmedFeedback) {
      setValidationError("Please enter rejection comments before saving a draft.");
      return;
    }

    setValidationError(null);
    await onSaveDraft({
      submissionId,
      reviewerId,
      feedbackComment: trimmedFeedback,
    });
  };

  const handleTextareaKeyDown = async (event: KeyboardEvent<HTMLTextAreaElement>) => {
    if (!(event.metaKey || event.ctrlKey) || event.key !== "Enter") {
      return;
    }

    event.preventDefault();
    await handleReject();
  };

  return (
    <section className="panel">
      <div className="panel-header">
        <h3>Decision</h3>
        <p>Approve qualified content, or reject with required feedback and optional draft saving.</p>
      </div>
      <div className="panel-body decision-panel">
        {initialDraft ? (
          <div className="draft-banner">
            Draft loaded from {formatDateTime(initialDraft.savedAt)} by {initialDraft.savedByReviewerName}.
          </div>
        ) : null}

        <div className="decision-grid">
          <button
            className="decision-option decision-option-approve"
            type="button"
            onClick={handleApprove}
            disabled={submitting || savingDraft}
          >
            <span className="decision-option-label">Approve</span>
            <strong>Publish the resource</strong>
            <p>Marks the submission as approved, removes it from the queue, and makes it visible.</p>
          </button>
          <button
            className="decision-option decision-option-reject"
            type="button"
            onClick={() => {
              setShowRejectInput((current) => !current);
              setValidationError(null);
            }}
            disabled={submitting || savingDraft}
          >
            <span className="decision-option-label">Reject</span>
            <strong>
              {initialDraft ? "Continue rejection draft" : "Request contributor changes"}
            </strong>
            <p>Requires reviewer comments and records the decision as a new history entry.</p>
          </button>
        </div>

        {showRejectInput ? (
          <>
            <label className="input-label">
              <span className="input-header">
                <span>Rejection comments</span>
                <span className="char-counter">{feedbackComment.length} / 800</span>
              </span>
              <textarea
                ref={textareaRef}
                value={feedbackComment}
                maxLength={800}
                onChange={(event) => {
                  setFeedbackComment(event.target.value);
                  if (validationError) {
                    setValidationError(null);
                  }
                }}
                onKeyDown={(event) => {
                  void handleTextareaKeyDown(event);
                }}
                placeholder="Explain what the contributor needs to fix before resubmission."
              />
            </label>

            <div className="draft-meta">
              <span className="helper-text">
                Required for rejection. Use Ctrl/Cmd + Enter to submit quickly.
              </span>
              {draftState === "dirty" ? <span className="inline-status warning">Unsaved changes</span> : null}
              {draftState === "saved" ? <span className="inline-status success">Draft synced</span> : null}
              {draftState === "empty" ? <span className="inline-status neutral">Draft not saved yet</span> : null}
            </div>

            {validationError ? <p className="error-text">{validationError}</p> : null}

            <div className="action-row">
              <button
                className="button secondary"
                type="button"
                onClick={() => {
                  void handleSaveDraft();
                }}
                disabled={submitting || savingDraft || draftState === "saved" || !trimmedFeedback}
              >
                {savingDraft ? "Saving draft..." : "Save Draft"}
              </button>
              <button
                className="button danger"
                type="button"
                onClick={() => {
                  void handleReject();
                }}
                disabled={submitting || savingDraft}
              >
                {submitting ? "Submitting..." : "Submit Rejection"}
              </button>
              <button
                className="button ghost"
                type="button"
                onClick={() => {
                  setShowRejectInput(false);
                  setFeedbackComment(initialDraft?.feedbackComment ?? "");
                  setValidationError(null);
                }}
                disabled={submitting || savingDraft}
              >
                Cancel
              </button>
            </div>
          </>
        ) : null}
      </div>
    </section>
  );
}
