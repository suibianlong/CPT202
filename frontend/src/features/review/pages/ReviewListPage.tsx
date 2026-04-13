import { useEffect, useMemo, useState } from "react";
import { ReviewListTable } from "../components/ReviewListTable";
import { PageResponse, ReviewListItem } from "../types";

interface ReviewListPageProps {
  listResponse: PageResponse<ReviewListItem> | null;
  loading: boolean;
  error: string | null;
  selectedSubmissionId: number | null;
  onSelectSubmission: (submissionId: number) => void;
}

type SortMode = "newest" | "oldest" | "title";

export function ReviewListPage({
  listResponse,
  loading,
  error,
  selectedSubmissionId,
  onSelectSubmission,
}: ReviewListPageProps) {
  const [searchTerm, setSearchTerm] = useState("");
  const [sortMode, setSortMode] = useState<SortMode>("newest");

  const items = listResponse?.items ?? [];

  const filteredItems = useMemo(() => {
    const normalizedTerm = searchTerm.trim().toLowerCase();

    const nextItems = normalizedTerm
      ? items.filter((item) =>
          [
            item.title,
            item.contributorName,
            item.categoryType,
            item.categoryTopic,
            `submission ${item.submissionId}`,
            `version ${item.versionNo}`,
          ]
            .join(" ")
            .toLowerCase()
            .includes(normalizedTerm),
        )
      : [...items];

    nextItems.sort((left, right) => {
      if (sortMode === "title") {
        return left.title.localeCompare(right.title);
      }

      const leftTime = new Date(left.submittedAt).getTime();
      const rightTime = new Date(right.submittedAt).getTime();
      return sortMode === "oldest" ? leftTime - rightTime : rightTime - leftTime;
    });

    return nextItems;
  }, [items, searchTerm, sortMode]);

  useEffect(() => {
    if (!filteredItems.length) {
      return;
    }

    const selectionStillVisible = filteredItems.some(
      (item) => item.submissionId === selectedSubmissionId,
    );

    if (!selectionStillVisible) {
      onSelectSubmission(filteredItems[0].submissionId);
    }
  }, [filteredItems, onSelectSubmission, selectedSubmissionId]);

  const activeItem =
    filteredItems.find((item) => item.submissionId === selectedSubmissionId) ??
    items.find((item) => item.submissionId === selectedSubmissionId) ??
    null;

  return (
    <section className="panel review-sidebar">
      <div className="panel-header">
        <h2>Pending Reviews</h2>
        <p>Review queue for submissions that are still awaiting a final approval decision.</p>
      </div>
      <div className="panel-body">
        {loading ? <div className="loading-state">Loading pending submissions...</div> : null}
        {!loading && error ? <div className="error-state">{error}</div> : null}
        {!loading && !error && listResponse && !items.length ? (
          <div className="empty-state">No submissions are currently awaiting review.</div>
        ) : null}
        {!loading && !error && listResponse && items.length ? (
          <div className="stack">
            <div className="review-toolbar">
              <label className="search-field">
                <span className="search-icon" aria-hidden="true">
                  Search
                </span>
                <span className="visually-hidden">Search pending submissions</span>
                <input
                  type="search"
                  value={searchTerm}
                  onChange={(event) => setSearchTerm(event.target.value)}
                  placeholder="Search by title, contributor, category, or submission ID"
                />
              </label>
              <div className="sort-toggle" role="group" aria-label="Sort pending submissions">
                <button
                  className={`filter-chip ${sortMode === "newest" ? "is-active" : ""}`}
                  type="button"
                  onClick={() => setSortMode("newest")}
                >
                  Newest first
                </button>
                <button
                  className={`filter-chip ${sortMode === "oldest" ? "is-active" : ""}`}
                  type="button"
                  onClick={() => setSortMode("oldest")}
                >
                  Oldest first
                </button>
                <button
                  className={`filter-chip ${sortMode === "title" ? "is-active" : ""}`}
                  type="button"
                  onClick={() => setSortMode("title")}
                >
                  Title A-Z
                </button>
              </div>
            </div>

            <div className="summary-grid">
              <article className="summary-card summary-card-primary">
                <span className="summary-card-label">Pending submissions</span>
                <strong>{items.length}</strong>
                <p>Only resources in Pending Review appear in this queue.</p>
              </article>
              <article className="summary-card summary-card-secondary">
                <span className="summary-card-label">Current focus</span>
                <strong>{activeItem ? `#${activeItem.submissionId}` : "None"}</strong>
                <p>
                  {activeItem
                    ? `${activeItem.contributorName} · ${activeItem.categoryTopic}`
                    : "Select a submission from the queue to inspect its full detail."}
                </p>
              </article>
            </div>

            <div className="summary-banner">
              <strong>{filteredItems.length}</strong>
              <span>
                {searchTerm.trim()
                  ? `matching result(s) from ${items.length} pending submission(s).`
                  : "pending submission(s) are ready for review."}
              </span>
              {searchTerm.trim() ? (
                <button className="button ghost small" type="button" onClick={() => setSearchTerm("")}>
                  Clear search
                </button>
              ) : null}
            </div>

            {!filteredItems.length ? (
              <div className="empty-state">
                No pending submissions match your current search. Try a different keyword.
              </div>
            ) : (
              <ReviewListTable
                items={filteredItems}
                selectedSubmissionId={selectedSubmissionId}
                onSelectSubmission={onSelectSubmission}
              />
            )}
          </div>
        ) : null}
      </div>
    </section>
  );
}
