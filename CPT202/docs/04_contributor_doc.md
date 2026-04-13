# #4 - Contributor Module Requirements (Week 1)

## Role Overview

**#4 Responsible for: Contributor Submission & Revision**

Enabling "resource contributors" to complete:
- Create draft → Submit → Wait for review → Edit and resubmit if rejected

---

## 1. Core Status Definitions (Contributor View)

| Status | English | Visibility | Can Do | Cannot Do |
|--------|---------|------------|--------|-----------|
| Draft | Draft | Author only | Create/Edit/Save | Cannot be reviewed / Not visible |
| Pending Review | Pending Review | Author sees "Pending" | Wait for review / View submission time | Usually cannot edit (unless撤回) |
| Approved | Approved | Author sees "Published" | View published content | Usually read-only |
| Rejected | Rejected | Author sees "Rejected" | **Must see rejection reason** / Edit and resubmit | Cannot go live directly |
| Archived | Archived | Author sees but marked "Archived" | View history only | Cannot resubmit |

---

## 2. Contributor Module Pages

### 2.1 Resource Form (CreateResource / EditResource)

**Features:**
- Create new resource (Draft status)
- Edit existing resource
- Save draft (without submitting)
- Submit for review (Pending Review status)

**Form Fields:**
- [ ] Title (title) - Required
- [ ] Category (category) - Required (linked to admin-managed category list)
- [ ] Place (place) - Optional
- [ ] Description (description) - Required, rich text editor
- [ ] Tags/Keywords (tags) - Optional
- [ ] File Upload (upload) - Supports images/documents
- [ ] External Link (external_link) - Optional
- [ ] Copyright (copyright) - Required

**Action Buttons:**
- `Save Draft` → Status becomes Draft
- `Submit for Review` → Status becomes Pending Review (validate required fields)

### 2.2 My Submissions (MyResources)

**Table Columns:**
| Column | Description |
|--------|-------------|
| Title | Resource title |
| Category | Resource category |
| Status | Current status (Draft/Pending/Rejected/Approved/Archived) |
| Submitted | Submission time |
| Last Updated | Last modification time |

**Filters:**
- Filter by status (All/Draft/Pending/Rejected/Approved/Archived)
- Sort by time

**Actions:**
- Click title → View detail page
- `Edit` → Edit draft or rejected resources
- `Delete` → Delete draft (pending not allowed)

### 2.3 Submission Detail (SubmissionDetail)

**Content:**
- Complete resource info (title, category, description, etc.)
- **Review Feedback Section (shown when Rejected)** ← Key: Must show review comments

**Rejected Status Display:**
```
+------------------------------------------+
| This resource was not approved            |
| Review Time: 2026-03-29 15:30            |
| Reviewer: Zhang San                       |
| Feedback:                                |
| - Description in paragraph 3 not detailed |
| - Image clarity insufficient              |
|                                          |
| [Edit and Resubmit]                      |
+------------------------------------------+
```

---

## 3. Status Flow Rules

### 3.1 Normal Flow

```
Draft → (Submit) → Pending Review
                       ↓
              +-------+-------+
              ↓               ↓
         Approved        Rejected
        (Published)      (Rejected)
              ↓               ↓
        Archived      (Edit & Resubmit)
         ↓                    ↓
        [*]              Pending Review
                              ↓
                          Approved
```

### 3.2 Status Transitions

| Current Status | Action | Target Status | Notes |
|---------------|--------|---------------|-------|
| Draft | Save Draft | Draft | - |
| Draft | Submit | Pending Review | Validate required fields |
| Pending Review | Approve | Approved | Reviewer action |
| Pending Review | Reject | Rejected | Reviewer action, must provide feedback |
| Rejected | Edit & Resubmit | Pending Review | Re-enter review queue |
| Approved | Archive | Archived | Admin action |

---

## 4. Alignment with Other Modules

| Module | Alignment | Status |
|--------|-----------|--------|
| #5 (Review Module) | Rejection feedback fields | Confirmed |
| #3 (Form Design) | Submit button API and behavior | Confirmed |
| #8 (Architecture) | Unified status enum naming | Pending |
| #6 (Browse Module) | Approved visible to users; Archived hidden | Confirmed |

---

## 5. Permission Control

- **Only Approved Contributors** can access submission features
- Non-contributors see guide page: "Apply to become a contributor"
- Unauthenticated users cannot access any submission pages

---

## 6. Week 1 Deliverables

- [x] Status definition document
- [x] Status flowchart (Mermaid)
- [x] Alignment records with #3/#5/#6
- [x] Basic component files

---

## Version History

| Version | Date | Changes |
|---------|------|---------|
| 0.1 | 2026-03-30 | Initial version |
