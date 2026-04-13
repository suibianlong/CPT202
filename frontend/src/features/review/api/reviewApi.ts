import {
  PageResponse,
  ReviewDecisionPayload,
  ReviewDecisionResponse,
  ReviewDetail,
  ReviewDraftPayload,
  ReviewDraftResponse,
  ReviewListItem,
} from "../types";

const API_BASE_URL =
  import.meta.env.VITE_REVIEW_API_BASE_URL ?? "http://localhost:8080/api/reviewer/reviews";

type ApiErrorResponse = {
  code?: string;
  message?: string;
  details?: Record<string, string>;
};

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
    ...init,
  });

  if (!response.ok) {
    let message = `Request failed with status ${response.status}.`;

    try {
      const errorBody = (await response.json()) as ApiErrorResponse;
      if (errorBody.message) {
        message = errorBody.message;
      }
      if (errorBody.details && Object.keys(errorBody.details).length > 0) {
        message = `${message} ${Object.values(errorBody.details).join(" ")}`.trim();
      }
    } catch {
      // Ignore parse issues and fall back to generic message.
    }

    throw new Error(message);
  }

  return (await response.json()) as T;
}

export function fetchPendingReviews(
  page = 1,
  pageSize = 10,
): Promise<PageResponse<ReviewListItem>> {
  return request<PageResponse<ReviewListItem>>(`/pending?page=${page}&pageSize=${pageSize}`);
}

export function fetchReviewDetail(submissionId: number): Promise<ReviewDetail> {
  return request<ReviewDetail>(`/submissions/${submissionId}`);
}

export function submitReviewDecision(
  payload: ReviewDecisionPayload,
): Promise<ReviewDecisionResponse> {
  return request<ReviewDecisionResponse>(`/submissions/${payload.submissionId}/decision`, {
    method: "POST",
    body: JSON.stringify(payload),
  });
}

export function saveRejectionDraft(
  payload: ReviewDraftPayload,
): Promise<ReviewDraftResponse> {
  return request<ReviewDraftResponse>(`/submissions/${payload.submissionId}/rejection-draft`, {
    method: "PUT",
    body: JSON.stringify(payload),
  });
}
