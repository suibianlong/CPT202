import { KeyboardEvent } from "react";
import { ReviewListItem } from "../types";
import { StatusBadge } from "./StatusBadge";

interface ReviewListTableProps {
  items: ReviewListItem[];
  selectedSubmissionId: number | null;
  onSelectSubmission: (submissionId: number) => void;
}

function formatDateTime(value: string) {
  return new Date(value).toLocaleString();
}

export function ReviewListTable({
  items,
  selectedSubmissionId,
  onSelectSubmission,
}: ReviewListTableProps) {
  const handleRowKeyDown = (
    event: KeyboardEvent<HTMLTableRowElement>,
    submissionId: number,
  ) => {
    if (event.key !== "Enter" && event.key !== " ") {
      return;
    }

    event.preventDefault();
    onSelectSubmission(submissionId);
  };

  return (
    <div className="list-table-wrap">
      <table className="list-table">
        <colgroup>
          <col className="list-col-title" />
          <col className="list-col-contributor" />
          <col className="list-col-category" />
          <col className="list-col-submitted" />
          <col className="list-col-status" />
        </colgroup>
        <thead>
          <tr>
            <th>Title</th>
            <th>Contributor</th>
            <th>Category / Topic</th>
            <th>Submitted</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          {items.map((item) => (
            <tr
              key={item.submissionId}
              className={`list-row ${selectedSubmissionId === item.submissionId ? "is-selected" : ""}`}
              onClick={() => onSelectSubmission(item.submissionId)}
              onKeyDown={(event) => handleRowKeyDown(event, item.submissionId)}
              role="button"
              tabIndex={0}
              aria-selected={selectedSubmissionId === item.submissionId}
            >
              <td>
                <div className="list-title-block">
                  <strong className="list-title" title={item.title}>
                    {item.title}
                  </strong>
                  <div className="list-meta-row">
                    <span className="inline-pill">Version {item.versionNo}</span>
                    <span className="helper-text">Submission #{item.submissionId}</span>
                  </div>
                </div>
              </td>
              <td>
                <span className="cell-clamp" title={item.contributorName}>
                  {item.contributorName}
                </span>
              </td>
              <td>
                <span
                  className="cell-clamp"
                  title={`${item.categoryType} / ${item.categoryTopic}`}
                >
                  {item.categoryType} / {item.categoryTopic}
                </span>
              </td>
              <td>
                <span className="cell-clamp" title={formatDateTime(item.submittedAt)}>
                  {formatDateTime(item.submittedAt)}
                </span>
              </td>
              <td>
                <StatusBadge status={item.resourceStatus} />
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
}
