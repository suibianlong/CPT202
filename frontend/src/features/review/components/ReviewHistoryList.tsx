import { useState } from "react";
import { ReviewHistoryItem } from "../types";
import { StatusBadge } from "./StatusBadge";

interface ReviewHistoryListProps {
  items: ReviewHistoryItem[];
}

interface HistoryCommentProps {
  feedbackComment?: string | null;
}

function formatDateTime(value: string) {
  return new Date(value).toLocaleString();
}

function HistoryComment({ feedbackComment }: HistoryCommentProps) {
  const [expanded, setExpanded] = useState(false);
  const text = feedbackComment?.trim() ?? "";

  if (!text) {
    return <p className="helper-text">No comments attached to this history entry.</p>;
  }

  const shouldClamp = text.length > 160;
  const displayText =
    !shouldClamp || expanded ? text : `${text.slice(0, 160).trimEnd()}...`;

  return (
    <div className="history-comment-block">
      <p className="history-comment">{displayText}</p>
      {shouldClamp ? (
        <button className="text-toggle" type="button" onClick={() => setExpanded((current) => !current)}>
          {expanded ? "Show less" : "Show more"}
        </button>
      ) : null}
    </div>
  );
}

export function ReviewHistoryList({ items }: ReviewHistoryListProps) {
  if (!items.length) {
    return <div className="empty-state">No previous review records for this resource.</div>;
  }

  return (
    <div className="history-list">
      <p className="helper-text">Chronological order: earliest decision to latest decision.</p>
      {items.map((item, index) => (
        <article key={item.reviewRecordId} className="history-item">
          <div className="history-item-layout">
            <div className="history-index">{String(index + 1).padStart(2, "0")}</div>
            <div className="history-main">
              <div className="history-meta">
                <span className={`context-pill ${item.contextType === "CURRENT_SUBMISSION" ? "current" : "previous"}`}>
                  {item.contextLabel}
                </span>
                <span>Reviewer: {item.reviewerName}</span>
                <span>Decision: {item.action}</span>
                <span>{formatDateTime(item.reviewedAt)}</span>
              </div>
              <StatusBadge status={item.status} />
              <HistoryComment feedbackComment={item.feedbackComment} />
            </div>
          </div>
        </article>
      ))}
    </div>
  );
}
