# #4 - Meeting Notes (Week 1)

## Meeting Agenda

### Topic 1: Submit Flow Confirmation with #3 (Form Design)
**Status: Confirmed**

Confirmed questions:
1. **What API does the submit button trigger?**
   - `POST /api/resources/:id/submit`
   - If there are unsaved changes, auto save first with `PUT /api/resources/:id`

2. **Need confirmation dialog before submit?** → **No**

3. **Where to redirect after successful submit?**
   - Stay on current page
   - Reload current resource and left sidebar submissions list
   - Status updates to Pending Review
   - Form becomes read-only

**Source:** #3

---

### Topic 2: Review Feedback Fields Confirmation with #5 (Review Module)
**Status: Confirmed**

Confirmed questions:
1. **Required fields when rejecting:**
   - `feedback` (rejection reason) → **Required**
   - `detailed_feedback` (detailed feedback) → Optional
   - `suggested_improvements` (suggested improvements) → Optional

2. **Can attach reference links after rejection?**
   - `reference_links` → Optional

**Source:** #5

---

### Topic 3: Visibility Rules Confirmation with #6 (Browse Module)
**Status: Confirmed**

Confirmed questions:
1. **Only APPROVED resources visible to regular users?** → **Yes**
2. **ARCHIVED resources hidden from everyone?** → **Yes**
3. Can contributors see their own APPROVED resources in "My Submissions"?
   - Yes (current implementation)

**Source:** #6

---

### Topic 4: Status Enum Confirmation with #8 (Architecture)
**Status: Pending**

Questions to confirm:
1. Status enum naming:
   - DRAFT / PENDING_REVIEW / APPROVED / REJECTED / ARCHIVED
2. Database field naming convention?

**Owner:** #4
**Awaiting:** #8

---

## Meeting Records

| Date | Topic | With | Conclusion | Action Item |
|------|-------|------|------------|-------------|
| 2026-03-30 | Status Definition | Team | Confirm 5 status definitions | Write docs |
| 2026-03-30 | Submit Flow | #3 | Confirm API and no-dialog logic | Code updated |
| 2026-03-30 | Review Feedback Fields | #5 | feedback required, others optional | Update model docs |
| 2026-03-30 | Visibility Rules | #6 | APPROVED visible, ARCHIVED hidden | No code changes needed |

---

## Implementation Status

| Module | Status | Description |
|--------|--------|-------------|
| Submit Flow | Done | Implemented per #3 logic |
| Rejection Feedback | Done | Implemented per #5 fields |
| Visibility Control | Done | Implemented per #6 rules |
| Status Enum | Pending | Awaiting #8 confirmation |

---

*Created: 2026-03-30*
*Last Updated: 2026-03-30*
