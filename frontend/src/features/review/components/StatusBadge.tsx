import { ResourceReviewStatus } from "../types";

interface StatusBadgeProps {
  status: ResourceReviewStatus;
}

const statusLabelMap: Record<ResourceReviewStatus, string> = {
  DRAFT: "Draft",
  PENDING_REVIEW: "Pending Review",
  APPROVED: "Approved",
  REJECTED: "Rejected",
  ARCHIVED: "Archived",
};

const statusClassMap: Record<ResourceReviewStatus, string> = {
  DRAFT: "default",
  PENDING_REVIEW: "pending",
  APPROVED: "approved",
  REJECTED: "rejected",
  ARCHIVED: "default",
};

export function StatusBadge({ status }: StatusBadgeProps) {
  return (
    <span className={`status-badge ${statusClassMap[status]}`}>{statusLabelMap[status]}</span>
  );
}
