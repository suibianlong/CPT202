import { useCallback, useEffect, useMemo, useState } from "react";
import {
  fetchReviewDetailMock,
  submitReviewDecisionMock,
} from "./mockData";
import "./review.css";
import type {
  ReviewDecisionPayload,
  ReviewDecisionResponse,
  ReviewDetail,
} from "./types";

type ReviewDetailPageProps = {
  submissionId?: number;
  reviewerId?: number;
  fetchReviewDetail?: (submissionId: number) => Promise<ReviewDetail>;
  submitReviewDecision?: (
    payload: ReviewDecisionPayload,
  ) => Promise<ReviewDecisionResponse>;
  onBack?: () => void;
  onDecisionSubmitted?: (response: ReviewDecisionResponse) => void;
};

const DEFAULT_SUBMISSION_ID = 301;

export function ReviewDetailPage({
  submissionId = DEFAULT_SUBMISSION_ID,
  reviewerId = 2,
  fetchReviewDetail = fetchReviewDetailMock,
  submitReviewDecision = submitReviewDecisionMock,
  onBack,
  onDecisionSubmitted,
}: ReviewDetailPageProps) {
  const [detail, setDetail] = useState<ReviewDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);
  const [decisionMessage, setDecisionMessage] = useState<string | null>(null);
  const [showRejectForm, setShowRejectForm] = useState(false);
  const [feedbackComment, setFeedbackComment] = useState("");

  const loadDetail = useCallback(async () => {
    try {
      setIsLoading(true);
      setErrorMessage(null);
      const response = await fetchReviewDetail(submissionId);
      setDetail(response);
    } catch (error) {
      setDetail(null);
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsLoading(false);
    }
  }, [fetchReviewDetail, submissionId]);

  useEffect(() => {
    setDecisionMessage(null);
    setShowRejectForm(false);
    setFeedbackComment("");
    void loadDetail();
  }, [loadDetail]);

  const isPending = detail?.resourceStatus === "PENDING_REVIEW";
  const latestFeedback = useMemo(
    () => detail?.reviewHistory.find((item) => item.feedbackComment)?.feedbackComment ?? "",
    [detail],
  );

  const handleApprove = async () => {
    if (!detail) {
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage(null);
      setDecisionMessage(null);

      const response = await submitReviewDecision({
        submissionId: detail.submissionId,
        resourceId: detail.resourceId,
        versionNo: detail.versionNo,
        reviewerId,
        action: "APPROVE",
      });

      setDecisionMessage("Submission approved successfully.");
      setShowRejectForm(false);
      setFeedbackComment("");
      onDecisionSubmitted?.(response);
      await loadDetail();
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleRejectSubmit = async () => {
    if (!detail) {
      return;
    }

    if (!feedbackComment.trim()) {
      setErrorMessage("Please provide rejection feedback before submitting.");
      return;
    }

    try {
      setIsSubmitting(true);
      setErrorMessage(null);
      setDecisionMessage(null);

      const response = await submitReviewDecision({
        submissionId: detail.submissionId,
        resourceId: detail.resourceId,
        versionNo: detail.versionNo,
        reviewerId,
        action: "REJECT",
        feedbackComment: feedbackComment.trim(),
      });

      setDecisionMessage("Submission rejected and feedback saved.");
      setShowRejectForm(false);
      setFeedbackComment("");
      onDecisionSubmitted?.(response);
      await loadDetail();
    } catch (error) {
      setErrorMessage(getErrorMessage(error));
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <section className="review-page">
      <div className="review-page__header">
        <div>
          <p className="review-page__eyebrow">Reviewer Workflow</p>
          <h1>Review Detail</h1>
          <p className="review-page__subtext">
            Check resource content, submission metadata, and previous decisions before approving or rejecting.
          </p>
        </div>
        {onBack ? (
          <button
            className="review-button review-button--secondary"
            type="button"
            onClick={onBack}
          >
            Back to list
          </button>
        ) : null}
      </div>

      {isLoading ? <div className="review-state">Loading review detail...</div> : null}
      {!isLoading && errorMessage ? (
        <div className="review-state review-state--error">{errorMessage}</div>
      ) : null}

      {!isLoading && detail ? (
        <div className="review-detail">
          {decisionMessage ? (
            <div className="review-state review-state--success">{decisionMessage}</div>
          ) : null}

          <div className="review-card">
            <div className="review-detail__title-row">
              <div>
                <h2>{detail.resource.title}</h2>
                <p className="review-page__subtext">
                  Submission #{detail.submissionId} • Version {detail.versionNo}
                </p>
              </div>
              <span className={`review-badge review-badge--${detail.resourceStatus.toLowerCase()}`}>
                {detail.resourceStatus}
              </span>
            </div>

            <div className="review-grid">
              <div>
                <h3>Resource Information</h3>
                <p>{detail.resource.description}</p>
                <p>
                  <strong>Place:</strong> {detail.resource.place || "Not provided"}
                </p>
                <p>
                  <strong>Resource type:</strong> {detail.resource.resourceType}
                </p>
                <p>
                  <strong>Contributor:</strong> {detail.contributor.username}
                </p>
              </div>

              <div>
                <h3>Category and Tags</h3>
                <p>
                  <strong>Category:</strong> {detail.category.categoryType}
                </p>
                <p>
                  <strong>Topic:</strong> {detail.category.categoryTopic}
                </p>
                <div className="review-tags">
                  {detail.tags.map((tag) => (
                    <span className="review-tag" key={tag}>
                      {tag}
                    </span>
                  ))}
                </div>
              </div>

              <div>
                <h3>Submission Information</h3>
                <p>
                  <strong>Submitted at:</strong> {formatDateTime(detail.submission.submittedAt)}
                </p>
                <p>
                  <strong>Submitted by:</strong> User #{detail.submission.submittedBy}
                </p>
                <p>
                  <strong>Submission note:</strong>{" "}
                  {detail.submission.submissionNote || "No note provided"}
                </p>
              </div>

              <div>
                <h3>Preview and Media</h3>
                {detail.resource.previewImage ? (
                  <img
                    className="review-preview"
                    src={detail.resource.previewImage}
                    alt={`${detail.resource.title} preview`}
                  />
                ) : (
                  <p>No preview image available.</p>
                )}
                {detail.resource.mediaUrl ? (
                  <p>
                    <a href={detail.resource.mediaUrl} target="_blank" rel="noreferrer">
                      Open resource link
                    </a>
                  </p>
                ) : (
                  <p>No media link available.</p>
                )}
              </div>
            </div>
          </div>

          <div className="review-card">
            <h3>Decision Panel</h3>
            <p className="review-page__subtext">
              Reject requires feedback. Approve can be submitted directly.
            </p>

            {!isPending ? (
              <p>
                This submission has already been reviewed. Latest feedback:{" "}
                {latestFeedback || "No feedback comment recorded."}
              </p>
            ) : (
              <>
                <div className="review-actions">
                  <button
                    className="review-button review-button--approve"
                    type="button"
                    disabled={isSubmitting}
                    onClick={() => void handleApprove()}
                  >
                    {isSubmitting ? "Submitting..." : "Approve"}
                  </button>
                  <button
                    className="review-button review-button--reject"
                    type="button"
                    disabled={isSubmitting}
                    onClick={() => setShowRejectForm((current) => !current)}
                  >
                    {showRejectForm ? "Hide reject form" : "Reject"}
                  </button>
                </div>

                {showRejectForm ? (
                  <div className="review-feedback">
                    <label htmlFor="feedbackComment">Rejection feedback *</label>
                    <textarea
                      id="feedbackComment"
                      value={feedbackComment}
                      onChange={(event) => setFeedbackComment(event.target.value)}
                      placeholder="Explain what the contributor needs to fix before resubmitting."
                      rows={5}
                    />
                    <div className="review-actions">
                      <button
                        className="review-button review-button--reject"
                        type="button"
                        disabled={isSubmitting}
                        onClick={() => void handleRejectSubmit()}
                      >
                        {isSubmitting ? "Submitting..." : "Submit rejection"}
                      </button>
                      <button
                        className="review-button review-button--secondary"
                        type="button"
                        disabled={isSubmitting}
                        onClick={() => {
                          setShowRejectForm(false);
                          setFeedbackComment("");
                        }}
                      >
                        Cancel
                      </button>
                    </div>
                  </div>
                ) : null}
              </>
            )}
          </div>

          <div className="review-card">
            <h3>Review History</h3>
            {detail.reviewHistory.length === 0 ? (
              <p>No previous review records.</p>
            ) : (
              <div className="review-history">
                {detail.reviewHistory.map((item) => (
                  <article className="review-history__item" key={item.reviewRecordId}>
                    <div className="review-history__row">
                      <strong>
                        {item.action} • {item.status}
                      </strong>
                      <span>{formatDateTime(item.reviewedAt)}</span>
                    </div>
                    <p>
                      Reviewer: {item.reviewerName} (#{item.reviewerId})
                    </p>
                    <p>{item.feedbackComment || "No feedback comment recorded."}</p>
                  </article>
                ))}
              </div>
            )}
          </div>
        </div>
      ) : null}
    </section>
  );
}

function formatDateTime(value: string): string {
  return new Date(value).toLocaleString();
}

function getErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Something went wrong.";
}
