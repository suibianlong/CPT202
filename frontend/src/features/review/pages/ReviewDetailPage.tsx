import { useState } from "react";
import { DecisionPanel } from "../components/DecisionPanel";
import { ReviewHistoryList } from "../components/ReviewHistoryList";
import { StatusBadge } from "../components/StatusBadge";
import { ReviewDetail, ReviewDraftPayload } from "../types";

interface ReviewDetailPageProps {
  detail: ReviewDetail | null;
  loading: boolean;
  error: string | null;
  submitting: boolean;
  savingDraft: boolean;
  onApprove: () => Promise<void>;
  onReject: (feedbackComment: string) => Promise<void>;
  onSaveDraft: (payload: ReviewDraftPayload) => Promise<void>;
}

interface ExpandableTextProps {
  value?: string | null;
  emptyText: string;
  clampAt?: number;
}

interface AssetLinkCardProps {
  label: string;
  url?: string | null;
  imagePreview?: boolean;
}

function formatDateTime(value: string | null | undefined) {
  if (!value) {
    return "Not reviewed yet";
  }

  return new Date(value).toLocaleString();
}

function jumpToSection(sectionId: string) {
  document.getElementById(sectionId)?.scrollIntoView({
    behavior: "smooth",
    block: "start",
  });
}

function ExpandableText({
  value,
  emptyText,
  clampAt = 180,
}: ExpandableTextProps) {
  const [expanded, setExpanded] = useState(false);
  const text = value?.trim() ?? "";

  if (!text) {
    return <span className="detail-value subdued">{emptyText}</span>;
  }

  const shouldClamp = text.length > clampAt;
  const displayText =
    !shouldClamp || expanded ? text : `${text.slice(0, clampAt).trimEnd()}...`;

  return (
    <div className="expandable-text">
      <p className="detail-value text-block">{displayText}</p>
      {shouldClamp ? (
        <button className="text-toggle" type="button" onClick={() => setExpanded((current) => !current)}>
          {expanded ? "Show less" : "Show more"}
        </button>
      ) : null}
    </div>
  );
}

function AssetLinkCard({ label, url, imagePreview = false }: AssetLinkCardProps) {
  const [copied, setCopied] = useState(false);
  const [previewVisible, setPreviewVisible] = useState(Boolean(imagePreview && url));

  const handleCopy = async () => {
    if (!url) {
      return;
    }

    try {
      if (navigator.clipboard?.writeText) {
        await navigator.clipboard.writeText(url);
        setCopied(true);
        window.setTimeout(() => setCopied(false), 1800);
      }
    } catch {
      setCopied(false);
    }
  };

  return (
    <article className="asset-card">
      <div className="section-heading compact">
        <h4>{label}</h4>
        {url ? <span className="inline-pill">External asset</span> : null}
      </div>

      {imagePreview && url && previewVisible ? (
        <div className="asset-preview">
          <img src={url} alt={`${label} preview`} onError={() => setPreviewVisible(false)} />
        </div>
      ) : (
        <div className="asset-preview asset-preview-placeholder">
          <span>{url ? "Preview unavailable for this asset" : "No asset provided"}</span>
        </div>
      )}

      {url ? (
        <>
          <a
            className="resource-link"
            href={url}
            target="_blank"
            rel="noreferrer"
            title={url}
          >
            {url}
          </a>
          <div className="inline-actions">
            <a className="button secondary small" href={url} target="_blank" rel="noreferrer">
              Open link
            </a>
            <button className="button ghost small" type="button" onClick={handleCopy}>
              {copied ? "Copied" : "Copy URL"}
            </button>
          </div>
        </>
      ) : (
        <span className="detail-value subdued">Not provided in this submission.</span>
      )}
    </article>
  );
}

export function ReviewDetailPage({
  detail,
  loading,
  error,
  submitting,
  savingDraft,
  onApprove,
  onReject,
  onSaveDraft,
}: ReviewDetailPageProps) {
  return (
    <div className="stack">
      <section className="panel">
        <div className="panel-header">
          <h2>Submission Detail</h2>
          <p>
            Review the metadata, linked assets, rights declarations, submission context, and
            review history before making a final decision.
          </p>
        </div>
        <div className="panel-body">
          {loading ? <div className="loading-state">Loading review detail...</div> : null}
          {!loading && error ? <div className="error-state">{error}</div> : null}
          {!loading && !error && !detail ? (
            <div className="placeholder">Select a pending submission to open its full reviewer view.</div>
          ) : null}
          {!loading && !error && detail ? (
            <div className="stack">
              <section className="detail-hero">
                <div className="detail-hero-header">
                  <div className="detail-hero-copy">
                    <p className="eyebrow">Submission #{detail.submissionId}</p>
                    <h3 className="detail-hero-title">{detail.resource.title}</h3>
                    <p className="detail-hero-subtitle">
                      {detail.category.categoryType} / {detail.category.categoryTopic} ·{" "}
                      {detail.resource.resourceType} · submitted by {detail.contributor.username}
                    </p>
                  </div>
                  <div className="hero-pill-row">
                    <StatusBadge status={detail.resourceStatus} />
                    <span className="hero-chip accent">Version {detail.versionNo}</span>
                    <span className="hero-chip">
                      {detail.resource.visibleToUsers ? "Publicly visible" : "Hidden until approval"}
                    </span>
                  </div>
                </div>

                <div className="hero-stat-grid hero-stat-grid-detail">
                  <div className="hero-stat-card">
                    <span className="hero-stat-label">Submitted At</span>
                    <strong>{formatDateTime(detail.submission.submittedAt)}</strong>
                  </div>
                  <div className="hero-stat-card">
                    <span className="hero-stat-label">Review History</span>
                    <strong>{detail.reviewHistory.length}</strong>
                  </div>
                  <div className="hero-stat-card">
                    <span className="hero-stat-label">Resubmission</span>
                    <strong>{detail.submission.resubmission ? "Yes" : "No"}</strong>
                  </div>
                </div>

                <div className="section-nav" aria-label="Jump to review sections">
                  <button className="nav-chip" type="button" onClick={() => jumpToSection("section-metadata")}>
                    Metadata
                  </button>
                  <button className="nav-chip" type="button" onClick={() => jumpToSection("section-assets")}>
                    Assets
                  </button>
                  <button className="nav-chip" type="button" onClick={() => jumpToSection("section-decision")}>
                    Decision
                  </button>
                  <button className="nav-chip" type="button" onClick={() => jumpToSection("section-history")}>
                    History
                  </button>
                </div>
              </section>

              {detail.submission.resubmission ? (
                <div className="context-banner">
                  This resource was revised and resubmitted. Previous review entries remain
                  preserved in history, while the current submission is assessed independently.
                </div>
              ) : null}

              <div className="detail-grid" id="section-metadata">
                <section className="detail-section">
                  <div className="section-heading">
                    <h3>Basic Metadata</h3>
                    <span className="inline-pill">Core fields</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Description</span>
                    <ExpandableText
                      value={detail.resource.description}
                      emptyText="No description provided"
                      clampAt={220}
                    />
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Place</span>
                    <span className="detail-value">{detail.resource.place ?? "Not provided"}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Contributor</span>
                    <span className="detail-value">{detail.contributor.username}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Last Reviewed At</span>
                    <span className="detail-value">{formatDateTime(detail.resource.reviewedAt)}</span>
                  </div>
                </section>

                <section className="detail-section">
                  <div className="section-heading">
                    <h3>Category & Keywords</h3>
                    <span className="inline-pill">Discovery metadata</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Category / Topic</span>
                    <span className="detail-value">
                      {detail.category.categoryType} / {detail.category.categoryTopic}
                    </span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Tags / Keywords</span>
                    <div className="tag-list">
                      {detail.tags.length ? (
                        detail.tags.map((tag) => (
                          <span className="tag" key={tag}>
                            {tag}
                          </span>
                        ))
                      ) : (
                        <span className="detail-value subdued">No tags provided.</span>
                      )}
                    </div>
                  </div>
                </section>

                <section className="detail-section">
                  <div className="section-heading">
                    <h3>Submission Context</h3>
                    <span className="inline-pill">Version tracking</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Context Summary</span>
                    <span className="detail-value">{detail.submission.currentContextLabel}</span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Submission Note</span>
                    <ExpandableText
                      value={detail.submission.submissionNote}
                      emptyText="No submission note provided"
                      clampAt={180}
                    />
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Created / Updated</span>
                    <span className="detail-value">
                      {formatDateTime(detail.resource.createdAt)} / {formatDateTime(detail.resource.updatedAt)}
                    </span>
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Status Snapshot</span>
                    <StatusBadge status={detail.submission.statusSnapshot} />
                  </div>
                </section>

                <section className="detail-section" id="section-assets">
                  <div className="section-heading">
                    <h3>Assets & Rights</h3>
                    <span className="inline-pill">Files, links, declarations</span>
                  </div>
                  <div className="asset-grid">
                    <AssetLinkCard
                      label="Preview Image"
                      url={detail.resource.previewImage}
                      imagePreview
                    />
                    <AssetLinkCard label="Media URL" url={detail.resource.mediaUrl} />
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Copyright Declaration</span>
                    <ExpandableText
                      value={detail.resource.copyrightDeclaration}
                      emptyText="No copyright declaration provided"
                      clampAt={200}
                    />
                  </div>
                  <div className="detail-row">
                    <span className="detail-label">Usage Declaration</span>
                    <ExpandableText
                      value={detail.resource.usageDeclaration}
                      emptyText="No usage declaration provided"
                      clampAt={200}
                    />
                  </div>
                </section>
              </div>
            </div>
          ) : null}
        </div>
      </section>

      {detail ? (
        <>
          <div id="section-decision">
            <DecisionPanel
              submitting={submitting}
              savingDraft={savingDraft}
              submissionId={detail.submissionId}
              reviewerId={2}
              initialDraft={detail.rejectionDraft}
              onApprove={onApprove}
              onReject={onReject}
              onSaveDraft={onSaveDraft}
            />
          </div>
          <section className="panel" id="section-history">
            <div className="panel-header">
              <h3>Review History</h3>
              <p>
                Decision records are appended in order and never overwrite previous outcomes. The
                timeline clearly distinguishes earlier submission rounds from the current one.
              </p>
            </div>
            <div className="panel-body">
              <ReviewHistoryList items={detail.reviewHistory} />
            </div>
          </section>
        </>
      ) : null}
    </div>
  );
}
