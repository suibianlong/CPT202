import { useEffect, useState } from "react";
import { fetchPendingReviewsMock } from "./mockData";
import "./review.css";
import type { PageResponse, ReviewListItem } from "./types";

type ReviewListPageProps = {
  fetchPendingReviews?: (
    page?: number,
    pageSize?: number,
  ) => Promise<PageResponse<ReviewListItem>>;
  onSelectSubmission?: (submissionId: number) => void;
};

export function ReviewListPage({
  fetchPendingReviews = fetchPendingReviewsMock,
  onSelectSubmission,
}: ReviewListPageProps) {
  const [data, setData] = useState<PageResponse<ReviewListItem>>({
    items: [],
    page: 1,
    pageSize: 10,
    total: 0,
  });
  const [isLoading, setIsLoading] = useState(true);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  useEffect(() => {
    let isMounted = true;

    const load = async () => {
      try {
        setIsLoading(true);
        setErrorMessage(null);
        const response = await fetchPendingReviews(1, 10);
        if (isMounted) {
          setData(response);
        }
      } catch (error) {
        if (isMounted) {
          setErrorMessage(getErrorMessage(error));
        }
      } finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    void load();

    return () => {
      isMounted = false;
    };
  }, [fetchPendingReviews]);

  return (
    <section className="review-page">
      <div className="review-page__header">
        <div>
          <p className="review-page__eyebrow">Reviewer Workflow</p>
          <h1>Pending Reviews</h1>
          <p className="review-page__subtext">
            Focus on the latest submissions that are still waiting for a decision.
          </p>
        </div>
        <div className="review-page__summary">
          <span className="review-page__summary-label">Pending items</span>
          <strong>{data.total}</strong>
        </div>
      </div>

      {isLoading ? (
        <div className="review-state">Loading pending submissions...</div>
      ) : null}

      {!isLoading && errorMessage ? (
        <div className="review-state review-state--error">{errorMessage}</div>
      ) : null}

      {!isLoading && !errorMessage && data.items.length === 0 ? (
        <div className="review-state">
          No submissions are currently awaiting review.
        </div>
      ) : null}

      {!isLoading && !errorMessage && data.items.length > 0 ? (
        <div className="review-table">
          <div className="review-table__row review-table__row--header">
            <span>Title</span>
            <span>Contributor</span>
            <span>Category / Topic</span>
            <span>Submitted</span>
            <span>Version</span>
            <span>Action</span>
          </div>

          {data.items.map((item) => (
            <div className="review-table__row" key={item.submissionId}>
              <span className="review-table__primary">{item.title}</span>
              <span>{item.contributorName}</span>
              <span>
                {item.categoryType} / {item.categoryTopic}
              </span>
              <span>{formatDateTime(item.submittedAt)}</span>
              <span>v{item.versionNo}</span>
              <span>
                <button
                  className="review-button review-button--secondary"
                  type="button"
                  onClick={() => onSelectSubmission?.(item.submissionId)}
                >
                  View detail
                </button>
              </span>
            </div>
          ))}
        </div>
      ) : null}
    </section>
  );
}

function formatDateTime(value: string): string {
  return new Date(value).toLocaleString();
}

function getErrorMessage(error: unknown): string {
  return error instanceof Error ? error.message : "Failed to load pending reviews.";
}
