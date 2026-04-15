<template>
  <div class="version-history-page">
    <div class="page-header">
      <button class="back-btn" @click="goBack">
        <span class="back-arrow">←</span> Back
      </button>
      <h2>Version History</h2>
      <p class="resource-title">{{ resourceTitle }}</p>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Loading version history...</p>
    </div>

    <div v-else-if="errorMessage" class="alert alert-error">
      {{ errorMessage }}
    </div>

    <div v-else class="content-area">
      <div class="version-list-panel">
        <h3>Versions</h3>
        <div class="version-list">
          <div
            v-for="version in versions"
            :key="version.submissionId"
            class="version-card"
            :class="{
              'selected': selectedVersion?.submissionId === version.submissionId,
              'current': version.isCurrent
            }"
            @click="selectVersion(version)"
          >
            <div class="version-header">
              <span class="version-number">v{{ version.versionNo.toFixed(1) }}</span>
              <span v-if="version.isCurrent" class="current-badge">Current</span>
              <span v-else-if="isRevisedVersion(version)" class="revised-badge">Revised</span>
            </div>
            <div class="version-meta">
              <span class="status" :class="getStatusClass(version.status)">
                {{ version.status }}
              </span>
              <span class="date">{{ formatDate(version.submittedAt) }}</span>
            </div>
            <div class="version-actions" v-if="!version.isCurrent">
              <button
                class="btn-compare"
                @click.stop="compareWithCurrent(version)"
                :disabled="comparing"
              >
                Compare
              </button>
              <button
                class="btn-rollback"
                @click.stop="confirmRollback(version)"
              >
                Rollback
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="version-detail-panel">
        <div v-if="!selectedVersion" class="empty-detail">
          <p>Select a version to view details</p>
        </div>

        <div v-else class="version-detail">
          <div class="detail-header">
            <h3>Version {{ selectedVersion.versionNo.toFixed(1) }}</h3>
            <span v-if="selectedVersion.isCurrent" class="current-badge">Current</span>
          </div>

          <div class="detail-section">
            <h4>Basic Info</h4>
            <div class="detail-row">
              <label>Title:</label>
              <span>{{ selectedVersion.title }}</span>
            </div>
            <div class="detail-row">
              <label>Category:</label>
              <span>{{ getCategoryName(selectedVersion) }}</span>
            </div>
            <div class="detail-row">
              <label>Location:</label>
              <span>{{ selectedVersion.place || '-' }}</span>
            </div>
          </div>

          <div class="detail-section">
            <h4>Descriptions</h4>
            <div class="detail-row">
              <label>Description:</label>
              <p class="multiline">{{ selectedVersion.description }}</p>
            </div>
            <div class="detail-row">
              <label>Copyright:</label>
              <p class="multiline">{{ selectedVersion.copyrightDeclaration }}</p>
            </div>
            <div class="detail-row" v-if="selectedVersion.usageDeclaration">
              <label>Usage:</label>
              <p class="multiline">{{ selectedVersion.usageDeclaration }}</p>
            </div>
          </div>

          <div class="detail-section" v-if="selectedVersion.tags?.length">
            <h4>Tags</h4>
            <div class="tags">
              <span v-for="tag in selectedVersion.tags" :key="tag" class="tag">{{ tag }}</span>
            </div>
          </div>

          <div class="detail-section">
            <h4>Review Feedback</h4>
            <div v-if="selectedVersion.reviewFeedback" class="review-feedback">
              <div class="feedback-item">
                <label>Reviewer:</label>
                <span>{{ selectedVersion.reviewerName }}</span>
              </div>
              <div class="feedback-item">
                <label>Status:</label>
                <span :class="getStatusClass(selectedVersion.status)">{{ selectedVersion.status }}</span>
              </div>
              <div class="feedback-item">
                <label>Comment:</label>
                <p class="multiline">{{ selectedVersion.reviewFeedback }}</p>
              </div>
            </div>
            <div v-else class="no-feedback">
              No review feedback for this version
            </div>
          </div>
        </div>
      </div>

      <!-- Version Comparison Modal -->
      <div v-if="showComparison" class="modal-overlay" @click.self="closeComparison">
        <div class="comparison-modal">
          <div class="modal-header">
            <h3>Version Comparison</h3>
            <button class="close-btn" @click="closeComparison">×</button>
          </div>
          <div class="comparison-content">
            <div class="comparison-legend">
              <span class="legend-item added">Added (green)</span>
              <span class="legend-item removed">Removed (red)</span>
            </div>
            <div class="comparison-grid">
              <div class="comparison-header">
                <div class="version-col">v{{ compareVersion?.versionNo.toFixed(1) }}</div>
                <div class="version-col">v{{ currentVersion?.versionNo.toFixed(1) }} (Current)</div>
              </div>
              <div class="comparison-body">
                <div class="comparison-row">
                  <div class="field-label">Title</div>
                  <div class="field-value" :class="getDiffClass('title', compareVersion?.title, currentVersion?.title)">
                    {{ compareVersion?.title || '-' }}
                  </div>
                  <div class="field-value" :class="getDiffClass('title', currentVersion?.title, compareVersion?.title)">
                    {{ currentVersion?.title || '-' }}
                  </div>
                </div>
                <div class="comparison-row">
                  <div class="field-label">Description</div>
                  <div class="field-value multiline" :class="getDiffClass('description', compareVersion?.description, currentVersion?.description)">
                    {{ compareVersion?.description || '-' }}
                  </div>
                  <div class="field-value multiline" :class="getDiffClass('description', currentVersion?.description, compareVersion?.description)">
                    {{ currentVersion?.description || '-' }}
                  </div>
                </div>
                <div class="comparison-row">
                  <div class="field-label">Copyright</div>
                  <div class="field-value" :class="getDiffClass('copyright', compareVersion?.copyrightDeclaration, currentVersion?.copyrightDeclaration)">
                    {{ compareVersion?.copyrightDeclaration || '-' }}
                  </div>
                  <div class="field-value" :class="getDiffClass('copyright', currentVersion?.copyrightDeclaration, compareVersion?.copyrightDeclaration)">
                    {{ currentVersion?.copyrightDeclaration || '-' }}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Rollback Confirmation Modal -->
      <div v-if="showRollbackConfirm" class="modal-overlay">
        <div class="modal rollback-modal">
          <h3>Confirm Rollback</h3>
          <p>Are you sure you want to rollback to version v{{ rollbackTarget?.versionNo.toFixed(1) }}?</p>
          <p class="warning">This will create a new version based on the selected version. The current version will be preserved in history.</p>
          
          <div class="rollback-changes" v-if="rollbackTarget">
            <h4>Changes that will be applied:</h4>
            <ul>
              <li v-if="currentVersion?.title !== rollbackTarget?.title">
                <span class="change-field">Title:</span> {{ rollbackTarget?.title }}
              </li>
              <li v-if="currentVersion?.description !== rollbackTarget?.description">
                <span class="change-field">Description:</span> Will be replaced
              </li>
              <li v-if="currentVersion?.copyrightDeclaration !== rollbackTarget?.copyrightDeclaration">
                <span class="change-field">Copyright Declaration:</span> Will be replaced
              </li>
            </ul>
          </div>

          <div class="modal-actions">
            <button class="btn-secondary" @click="showRollbackConfirm = false">Cancel</button>
            <button class="btn-danger" @click="performRollback" :disabled="loading">
              {{ loading ? 'Rolling back...' : 'Confirm Rollback' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

const API_BASE = '/api/resources'

export default {
  name: 'VersionHistory',
  data() {
    return {
      loading: false,
      errorMessage: '',
      resourceTitle: '',
      resourceId: null,
      versions: [],
      selectedVersion: null,
      currentVersion: null,
      showComparison: false,
      compareVersion: null,
      showRollbackConfirm: false,
      rollbackTarget: null,
      comparing: false
    }
  },
  created() {
    this.resourceId = this.$route.params.id
    this.loadVersionHistory()
  },
  methods: {
    async loadVersionHistory() {
      this.loading = true
      this.errorMessage = ''

      try {
        // Load current resource to get title
        const resourceRes = await axios.get(`${API_BASE}/${this.resourceId}`)
        this.resourceTitle = resourceRes.data.title
        this.currentVersion = this.transformResourceData(resourceRes.data)
        this.currentVersion.isCurrent = true

        // Load version history
        const historyRes = await axios.get(`${API_BASE}/${this.resourceId}/versions`)
        const historyVersions = historyRes.data || []

        // Transform and mark current version
        this.versions = historyVersions.map((v, index) => ({
          ...this.transformSubmissionData(v),
          isCurrent: index === 0 // First one is the latest/current
        }))

        // Add current version if not in history
        if (this.versions.length === 0 || !this.versions[0]?.isCurrent) {
          this.versions.unshift(this.currentVersion)
        }

        // Select the current version by default
        this.selectedVersion = this.currentVersion

      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Failed to load version history'
        console.error('Load version history error:', error)
      } finally {
        this.loading = false
      }
    },

    transformResourceData(resource) {
      return {
        submissionId: null,
        versionNo: resource.versionNo || 1,
        title: resource.title,
        description: resource.description,
        place: resource.place,
        categoryName: resource.categories?.[0]?.category_topic || '-',
        copyrightDeclaration: resource.copyright_declaration,
        usageDeclaration: resource.usage_declaration,
        tags: resource.tags?.map(t => t.tag_name) || [],
        status: resource.status,
        submittedAt: resource.last_submitted_time || resource.created_time,
        reviewFeedback: resource.feedback || null,
        reviewerName: resource.reviewerName || null,
        isCurrent: true
      }
    },

    transformSubmissionData(submission) {
      return {
        submissionId: submission.submissionId || submission.submission_id,
        versionNo: submission.versionNo || submission.version_no || 1,
        title: submission.title,
        description: submission.description,
        place: submission.place,
        categoryName: submission.categoryName || '-',
        copyrightDeclaration: submission.copyrightDeclaration || submission.copyright_declaration,
        usageDeclaration: submission.usageDeclaration || submission.usage_declaration,
        tags: submission.tags || [],
        status: submission.statusSnapshot || submission.status,
        submittedAt: submission.submittedAt || submission.submitted_at,
        reviewFeedback: submission.reviewFeedback || submission.feedback_comment,
        reviewerName: submission.reviewerName || submission.reviewer?.name || null,
        isCurrent: false
      }
    },

    selectVersion(version) {
      this.selectedVersion = version
    },

    isRevisedVersion(version) {
      // A version is "revised" if it's a resubmission after rejection
      // Check if there's a rejected status in history before this version
      const index = this.versions.findIndex(v => v.submissionId === version.submissionId)
      if (index < 0) return false
      
      for (let i = index + 1; i < this.versions.length; i++) {
        if (this.versions[i].status === 'REJECTED') {
          return true
        }
      }
      return false
    },

    compareWithCurrent(version) {
      this.compareVersion = version
      this.showComparison = true
    },

    closeComparison() {
      this.showComparison = false
      this.compareVersion = null
    },

    getDiffClass(field, newValue, oldValue) {
      if (!oldValue && newValue) return 'added'
      if (oldValue && newValue && oldValue !== newValue) return 'removed'
      return ''
    },

    confirmRollback(version) {
      this.rollbackTarget = version
      this.showRollbackConfirm = true
    },

    async performRollback() {
      if (!this.rollbackTarget) return

      this.loading = true
      this.errorMessage = ''

      try {
        // Call rollback API
        await axios.post(`${API_BASE}/${this.resourceId}/rollback`, {
          targetVersionNo: this.rollbackTarget.versionNo
        })

        this.showRollbackConfirm = false
        this.rollbackTarget = null

        // Reload version history
        await this.loadVersionHistory()

        alert('Rollback successful! A new version has been created.')
      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Failed to rollback'
        console.error('Rollback error:', error)
      } finally {
        this.loading = false
      }
    },

    getStatusClass(status) {
      const statusMap = {
        'APPROVED': 'status-approved',
        'REJECTED': 'status-rejected',
        'PENDING_REVIEW': 'status-pending',
        'DRAFT': 'status-draft',
        'Approved': 'status-approved',
        'Rejected': 'status-rejected',
        'Pending Review': 'status-pending',
        'Draft': 'status-draft'
      }
      return statusMap[status] || ''
    },

    getCategoryName(version) {
      return version.categoryName || '-'
    },

    formatDate(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleDateString() + ' ' + date.toLocaleTimeString([], { 
        hour: '2-digit', 
        minute: '2-digit' 
      })
    },

    goBack() {
      this.$router.push(`/contributor/detail/${this.resourceId}`)
    }
  }
}
</script>

<style scoped>
.version-history-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 24px;
}

.back-btn {
  background: none;
  border: none;
  color: #666;
  font-size: 14px;
  cursor: pointer;
  padding: 8px 0;
  margin-bottom: 8px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.back-btn:hover {
  color: #333;
}

.back-arrow {
  font-size: 18px;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.resource-title {
  color: #666;
  font-size: 14px;
  margin: 0;
}

.loading-state {
  text-align: center;
  padding: 60px 20px;
  color: #666;
}

.spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #f0f0f0;
  border-top-color: #1a1a1a;
  border-radius: 50%;
  animation: spin 1s linear infinite;
  margin: 0 auto 16px;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.alert {
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 20px;
  font-size: 14px;
}

.alert-error {
  background: #ffe6e6;
  color: #cc0000;
  border: 1px solid #ffcccc;
}

.content-area {
  display: grid;
  grid-template-columns: 350px 1fr;
  gap: 24px;
}

.version-list-panel {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  max-height: calc(100vh - 200px);
  overflow-y: auto;
}

.version-list-panel h3 {
  margin: 0 0 16px 0;
  font-size: 16px;
  color: #333;
}

.version-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.version-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.version-card:hover {
  border-color: #ddd;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.version-card.selected {
  border-color: #1a1a1a;
  background: #fff;
}

.version-card.current {
  border-left: 3px solid #22863a;
}

.version-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 8px;
}

.version-number {
  font-weight: 600;
  font-size: 15px;
  color: #1a1a1a;
}

.current-badge {
  background: #22863a;
  color: #fff;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 500;
  text-transform: uppercase;
}

.revised-badge {
  background: #0066cc;
  color: #fff;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 10px;
  font-weight: 500;
  text-transform: uppercase;
}

.version-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
  margin-bottom: 8px;
}

.status {
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
}

.status-approved, .status.status-approved { background: #e6f7e6; color: #22863a; }
.status-rejected, .status.status-rejected { background: #ffe6e6; color: #cc0000; }
.status-pending, .status.status-pending { background: #e6f0ff; color: #0066cc; }
.status-draft, .status.status-draft { background: #f0f0f0; color: #666; }

.version-actions {
  display: flex;
  gap: 6px;
  margin-top: 8px;
}

.btn-compare, .btn-rollback {
  flex: 1;
  padding: 6px 8px;
  border-radius: 4px;
  font-size: 12px;
  cursor: pointer;
  border: none;
  transition: all 0.2s;
}

.btn-compare {
  background: #f0f0f0;
  color: #333;
}

.btn-compare:hover:not(:disabled) {
  background: #e0e0e0;
}

.btn-compare:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-rollback {
  background: #0066cc;
  color: #fff;
}

.btn-rollback:hover {
  background: #0055aa;
}

.version-detail-panel {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 24px;
  min-height: 400px;
}

.empty-detail {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
}

.detail-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #eee;
}

.detail-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1a1a1a;
}

.detail-section {
  margin-bottom: 24px;
}

.detail-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #666;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.detail-row {
  margin-bottom: 12px;
}

.detail-row label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
  font-weight: 500;
}

.detail-row span {
  font-size: 14px;
  color: #333;
}

.detail-row p.multiline {
  margin: 0;
  font-size: 14px;
  color: #333;
  line-height: 1.6;
  white-space: pre-wrap;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.tag {
  background: #f0f0f0;
  color: #666;
  padding: 4px 10px;
  border-radius: 3px;
  font-size: 13px;
}

.review-feedback {
  background: #fafafa;
  padding: 16px;
  border-radius: 6px;
}

.feedback-item {
  margin-bottom: 12px;
}

.feedback-item:last-child {
  margin-bottom: 0;
}

.feedback-item label {
  display: inline-block;
  min-width: 80px;
}

.no-feedback {
  color: #999;
  font-style: italic;
  font-size: 14px;
}

/* Modal Styles */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.comparison-modal {
  background: #fff;
  border-radius: 8px;
  max-width: 900px;
  width: 90%;
  max-height: 90vh;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-bottom: 1px solid #eee;
}

.modal-header h3 {
  margin: 0;
  font-size: 18px;
  color: #1a1a1a;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  color: #999;
  cursor: pointer;
  padding: 0;
  line-height: 1;
}

.close-btn:hover {
  color: #333;
}

.comparison-content {
  padding: 24px;
  overflow-y: auto;
  flex: 1;
}

.comparison-legend {
  display: flex;
  gap: 24px;
  margin-bottom: 16px;
  font-size: 12px;
}

.legend-item {
  padding: 4px 8px;
  border-radius: 3px;
}

.legend-item.added {
  background: #e6f7e6;
  color: #22863a;
}

.legend-item.removed {
  background: #ffe6e6;
  color: #cc0000;
}

.comparison-grid {
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
}

.comparison-header {
  display: grid;
  grid-template-columns: 1fr 1fr;
  background: #fafafa;
  font-weight: 600;
  font-size: 14px;
}

.version-col {
  padding: 12px 16px;
  text-align: center;
  border-bottom: 1px solid #eee;
}

.version-col:first-child {
  border-right: 1px solid #eee;
}

.comparison-body {
  max-height: 400px;
  overflow-y: auto;
}

.comparison-row {
  display: grid;
  grid-template-columns: 120px 1fr 1fr;
  border-bottom: 1px solid #f0f0f0;
}

.comparison-row:last-child {
  border-bottom: none;
}

.field-label {
  padding: 12px 16px;
  font-size: 12px;
  font-weight: 500;
  color: #666;
  background: #fafafa;
  border-right: 1px solid #eee;
}

.field-value {
  padding: 12px 16px;
  font-size: 14px;
  color: #333;
}

.field-value:first-of-type {
  border-right: 1px solid #eee;
}

.field-value.multiline {
  white-space: pre-wrap;
  line-height: 1.5;
}

.field-value.added {
  background: #e6f7e6;
}

.field-value.removed {
  background: #ffe6e6;
}

.modal {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  max-width: 500px;
  width: 90%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #1a1a1a;
}

.modal p {
  margin: 0 0 16px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.modal .warning {
  background: #fff5f5;
  border: 1px solid #ffcccc;
  padding: 12px;
  border-radius: 4px;
  color: #cc0000;
}

.rollback-changes {
  margin: 16px 0;
  padding: 16px;
  background: #fafafa;
  border-radius: 6px;
}

.rollback-changes h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #333;
}

.rollback-changes ul {
  margin: 0;
  padding-left: 20px;
}

.rollback-changes li {
  font-size: 13px;
  color: #666;
  margin-bottom: 6px;
}

.change-field {
  font-weight: 500;
  color: #333;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  margin-top: 20px;
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-secondary:hover {
  background: #e0e0e0;
}

.btn-danger {
  background: #cc0000;
  color: #fff;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-danger:hover:not(:disabled) {
  background: #ff0000;
}

.btn-danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
