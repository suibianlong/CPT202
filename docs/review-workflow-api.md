# Review Workflow API Contract

## Recommended Decisions

- `resource.status` is the only source of truth for the current resource state.
- `resource_submission` represents the version currently being reviewed.
- `review_record` stores immutable decision history.
- `review_record.feedback_comment` is the rejection reason and optional approval note.
- `comment` is reserved for future discussion/comment threads and is not part of the review decision submission.
- `Feedback` and `Attached_File` are excluded from the core review workflow because their current schema is inconsistent.
- API always exposes `resourceId` as the resource primary key. If your physical column is `id` or `resource_id`, map it in the backend repository layer only.

## Status Mapping

Use normalized API enums and map them to database strings in the backend service layer.

| API enum | DB value |
| --- | --- |
| `DRAFT` | `Draft` |
| `PENDING_REVIEW` | `Pending Review` |
| `APPROVED` | `Approved` |
| `REJECTED` | `Rejected` |
| `ARCHIVED` | `Archived` |

## Shared DTOs

Shared frontend contract lives in [review-contract.ts](/Users/hejunyan/项目/cpt202/contracts/review-contract.ts).

## 1. Review List API

### Route

`GET /api/reviewer/reviews/pending`

### Query Params

| Field | Type | Required | Default | Notes |
| --- | --- | --- | --- | --- |
| `page` | `number` | No | `1` | 1-based page index |
| `pageSize` | `number` | No | `10` | recommend max `50` |

### Response DTO

```ts
PageResponse<ReviewListItem>
```

### Example Response

```json
{
  "items": [
    {
      "submissionId": 301,
      "resourceId": 1001,
      "versionNo": 2,
      "title": "Campus Study Space Guide",
      "contributorId": 18,
      "contributorName": "alice",
      "categoryId": 7,
      "categoryType": "Places",
      "categoryTopic": "Study Space",
      "submittedAt": "2026-04-08T09:30:00",
      "resourceStatus": "PENDING_REVIEW"
    }
  ],
  "page": 1,
  "pageSize": 10,
  "total": 1
}
```

### Backend Query Rules

- Filter by `resource.status = 'Pending Review'`
- Return only the latest submission version for each pending resource
- Join these tables:
  - `resource`
  - `resource_submission`
  - `user`
  - `category`

### Suggested SQL Shape

```sql
SELECT
  rs.submission_id,
  r.id AS resource_id,
  rs.version_no,
  r.title,
  u.user_id AS contributor_id,
  u.username AS contributor_name,
  c.category_id,
  c.category_type,
  c.category_topic,
  rs.submitted_at,
  r.status
FROM resource r
JOIN (
  SELECT resource_id, MAX(version_no) AS latest_version_no
  FROM resource_submission
  GROUP BY resource_id
) latest ON latest.resource_id = r.id
JOIN resource_submission rs
  ON rs.resource_id = latest.resource_id
 AND rs.version_no = latest.latest_version_no
JOIN user u ON u.user_id = rs.submitted_by
JOIN category c ON c.category_id = r.category_id
WHERE r.status = 'Pending Review'
ORDER BY rs.submitted_at ASC;
```

## 2. Review Detail API

### Route

`GET /api/reviewer/reviews/submissions/{submissionId}`

### Path Param

| Field | Type | Required | Notes |
| --- | --- | --- | --- |
| `submissionId` | `number` | Yes | identifies the exact submitted version |

### Response DTO

```ts
ReviewDetail
```

### Example Response

```json
{
  "submissionId": 301,
  "resourceId": 1001,
  "versionNo": 2,
  "resourceStatus": "PENDING_REVIEW",
  "resource": {
    "title": "Campus Study Space Guide",
    "description": "A guide to the best quiet areas on campus.",
    "place": "Library Building A",
    "resourceType": "LINK",
    "previewImage": "https://cdn.example.com/resource/1001/preview.png",
    "mediaUrl": "https://example.com/study-spaces",
    "createdAt": "2026-03-20T10:00:00",
    "updatedAt": "2026-04-08T09:30:00",
    "reviewedAt": null
  },
  "contributor": {
    "userId": 18,
    "username": "alice"
  },
  "category": {
    "categoryId": 7,
    "categoryType": "Places",
    "categoryTopic": "Study Space"
  },
  "submission": {
    "submittedAt": "2026-04-08T09:30:00",
    "submittedBy": 18,
    "submissionNote": "Updated link and preview image.",
    "statusSnapshot": "PENDING_REVIEW"
  },
  "tags": [
    "campus",
    "study"
  ],
  "reviewHistory": [
    {
      "reviewRecordId": 200,
      "resourceId": 1001,
      "submissionId": 288,
      "versionNo": 1,
      "reviewerId": 2,
      "reviewerName": "reviewer_anna",
      "action": "REJECT",
      "status": "REJECTED",
      "feedbackComment": "Please replace the broken media link.",
      "reviewedAt": "2026-03-21T14:00:00"
    }
  ]
}
```

### Backend Query Rules

- Find the exact row from `resource_submission` by `submission_id`
- Join these tables:
  - `resource`
  - `resource_submission`
  - `user`
  - `category`
  - `resource_tag`
  - `tag`
  - `review_record`
- `reviewHistory` should be ordered by `reviewed_at DESC`
- `reviewerName` can be resolved by joining `review_record.reviewer_id = user.user_id`

## 3. Review Decision API

### Route

`POST /api/reviewer/reviews/submissions/{submissionId}/decision`

### Request DTO

```ts
ReviewDecisionPayload
```

### Request Example: Approve

```json
{
  "submissionId": 301,
  "resourceId": 1001,
  "versionNo": 2,
  "reviewerId": 2,
  "action": "APPROVE"
}
```

### Request Example: Reject

```json
{
  "submissionId": 301,
  "resourceId": 1001,
  "versionNo": 2,
  "reviewerId": 2,
  "action": "REJECT",
  "feedbackComment": "Please clarify the source and replace the preview image."
}
```

### Response DTO

```ts
ReviewDecisionResponse
```

### Example Response

```json
{
  "reviewRecordId": 245,
  "submissionId": 301,
  "resourceId": 1001,
  "versionNo": 2,
  "action": "REJECT",
  "resourceStatus": "REJECTED",
  "feedbackComment": "Please clarify the source and replace the preview image.",
  "reviewedAt": "2026-04-08T11:10:00",
  "removedFromPendingQueue": true
}
```

### Validation Rules

- `submissionId` in path and body must match, otherwise return `400`
- `resourceId`, `versionNo`, `reviewerId`, `action` are required
- when `action = REJECT`, `feedbackComment` is required and cannot be blank
- when current `resource.status != 'Pending Review'`, return `409 STATUS_CONFLICT`
- when `submissionId` does not exist, return `404 NOT_FOUND`

### Backend Write Rules

When `action = APPROVE`:

1. insert a new `review_record`
2. set:
   - `action_description = 'APPROVE'`
   - `status = 'APPROVED'`
   - `feedback_comment = null` or an optional note
   - `reviewed_at = now`
3. update `resource`
4. set:
   - `status = 'Approved'`
   - `reviewed_at = now`
   - `updated_at = now`

When `action = REJECT`:

1. insert a new `review_record`
2. set:
   - `action_description = 'REJECT'`
   - `status = 'REJECTED'`
   - `feedback_comment = request.feedbackComment`
   - `reviewed_at = now`
3. update `resource`
4. set:
   - `status = 'Rejected'`
   - `reviewed_at = now`
   - `updated_at = now`

## Suggested Backend DTOs

If your backend is Java/Spring, these request and response DTOs are enough for the reviewer workflow:

```java
public record ResourceSection(
    String title,
    String description,
    String place,
    String resourceType,
    String previewImage,
    String mediaUrl,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime reviewedAt
) {}

public record ContributorSection(
    Long userId,
    String username
) {}

public record CategorySection(
    Long categoryId,
    String categoryType,
    String categoryTopic
) {}

public record SubmissionSection(
    LocalDateTime submittedAt,
    Long submittedBy,
    String submissionNote,
    String statusSnapshot
) {}

public record ReviewHistoryItemResponse(
    Long reviewRecordId,
    Long resourceId,
    Long submissionId,
    Integer versionNo,
    Long reviewerId,
    String reviewerName,
    String action,
    String status,
    String feedbackComment,
    LocalDateTime reviewedAt
) {}

public record ReviewListItemResponse(
    Long submissionId,
    Long resourceId,
    Integer versionNo,
    String title,
    Long contributorId,
    String contributorName,
    Long categoryId,
    String categoryType,
    String categoryTopic,
    LocalDateTime submittedAt,
    String resourceStatus
) {}

public record ReviewDetailResponse(
    Long submissionId,
    Long resourceId,
    Integer versionNo,
    String resourceStatus,
    ResourceSection resource,
    ContributorSection contributor,
    CategorySection category,
    SubmissionSection submission,
    List<String> tags,
    List<ReviewHistoryItemResponse> reviewHistory
) {}

public record ReviewDecisionRequest(
    Long submissionId,
    Long resourceId,
    Integer versionNo,
    Long reviewerId,
    String action,
    String feedbackComment
) {}

public record ReviewDecisionResponse(
    Long reviewRecordId,
    Long submissionId,
    Long resourceId,
    Integer versionNo,
    String action,
    String resourceStatus,
    String feedbackComment,
    LocalDateTime reviewedAt,
    boolean removedFromPendingQueue
) {}
```

## Error Response

```json
{
  "code": "STATUS_CONFLICT",
  "message": "This submission is no longer pending review.",
  "details": {
    "submissionId": "301",
    "currentStatus": "APPROVED"
  }
}
```

## Current Scope Boundary

- included:
  - pending review list
  - review detail
  - approve button
  - reject button
  - rejection feedback validation
  - review history read model
- excluded:
  - feedback attachments
  - general comment thread CRUD
  - reviewer reassignment
  - multi-step approval
