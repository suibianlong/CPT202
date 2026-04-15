<template>
  <div class="drafts-page">
    <div class="page-header">
      <h2>My Drafts</h2>
      <p class="subtitle">Manage your saved resource drafts</p>
    </div>

    <div v-if="loading" class="loading-state">
      <div class="spinner"></div>
      <p>Loading drafts...</p>
    </div>

    <div v-else-if="errorMessage" class="alert alert-error">
      {{ errorMessage }}
    </div>

    <div v-else-if="drafts.length === 0" class="empty-state">
      <div class="empty-icon">📝</div>
      <h3>No Drafts Yet</h3>
      <p>You haven't saved any drafts. Start creating a resource to see your drafts here.</p>
      <router-link to="/contributor/create" class="btn-primary">
        Create New Resource
      </router-link>
    </div>

    <div v-else class="drafts-list">
      <div
        v-for="draft in drafts"
        :key="draft.resourceId"
        class="draft-card"
        :class="{ 'has-error': draft.error }"
      >
        <div class="draft-header">
          <div class="draft-title">
            <h3>{{ draft.title || 'Untitled Draft' }}</h3>
            <span class="status-badge">Draft</span>
          </div>
          <div class="draft-actions">
            <button
              class="btn-secondary"
              @click="continueEditing(draft)"
              :disabled="loading"
            >
              Continue Editing
            </button>
            <button
              class="btn-danger"
              @click="confirmDelete(draft)"
              :disabled="loading"
            >
              Delete
            </button>
          </div>
        </div>

        <div class="draft-meta">
          <div class="meta-item">
            <span class="label">Last saved:</span>
            <span class="value">{{ formatTime(draft.lastSavedAt) }}</span>
          </div>
          <div class="meta-item">
            <span class="label">Category:</span>
            <span class="value">{{ getCategoryName(draft) }}</span>
          </div>
          <div v-if="draft.description" class="meta-item description">
            <span class="label">Description:</span>
            <span class="value">{{ truncate(draft.description, 100) }}</span>
          </div>
        </div>

        <div v-if="draft.error" class="error-notice">
          <strong>Auto-save failed:</strong> {{ draft.error }}
          <button class="btn-retry" @click="retrySave(draft)">Retry</button>
        </div>

        <div class="auto-save-indicator" v-if="draft.autoSaveInProgress">
          <span class="pulse"></span>
          Auto-saving...
        </div>
      </div>
    </div>

    <div v-if="showDeleteConfirm" class="modal-overlay">
      <div class="modal">
        <h3>Confirm Delete</h3>
        <p>Are you sure you want to delete the draft "{{ deleteTarget?.title || 'Untitled' }}"? This cannot be undone.</p>
        <div class="modal-actions">
          <button class="btn-secondary" @click="showDeleteConfirm = false">Cancel</button>
          <button class="btn-danger" @click="deleteDraft">Delete</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { getAllDraftsMetadata, removeDraft } from '../utils/localStorage'
import { clearChanges } from '../utils/autoSave'

export default {
  name: 'DraftsPage',
  data() {
    return {
      drafts: [],
      loading: false,
      errorMessage: '',
      showDeleteConfirm: false,
      deleteTarget: null
    }
  },
  created() {
    this.loadDrafts()
  },
  beforeRouteLeave(to, from, next) {
    // Check if there are unsaved changes in current editing session
    const hasUnsaved = this.checkUnsavedChanges()
    if (hasUnsaved) {
      const confirmLeave = confirm('You have unsaved changes. Are you sure you want to leave?')
      if (!confirmLeave) {
        next(false)
        return
      }
    }
    next()
  },
  methods: {
    loadDrafts() {
      this.loading = true
      this.errorMessage = ''

      try {
        const drafts = getAllDraftsMetadata()
        this.drafts = drafts.map(draft => ({
          ...draft,
          autoSaveInProgress: false,
          error: null
        }))
      } catch (error) {
        this.errorMessage = 'Failed to load drafts'
        console.error('Load drafts error:', error)
      } finally {
        this.loading = false
      }
    },

    continueEditing(draft) {
      // Clear any pending changes before navigating
      clearChanges()
      this.$router.push(`/contributor/edit/${draft.resourceId}`)
    },

    confirmDelete(draft) {
      this.deleteTarget = draft
      this.showDeleteConfirm = true
    },

    async deleteDraft() {
      if (!this.deleteTarget) return

      this.loading = true
      this.errorMessage = ''

      try {
        // First delete from backend if resource exists
        if (this.deleteTarget.resourceId) {
          await this.$axios.delete(`/api/resources/${this.deleteTarget.resourceId}`)
        }

        // Then clear from localStorage
        removeDraft(this.deleteTarget.resourceId)
        clearChanges()

        // Remove from local list
        this.drafts = this.drafts.filter(d => d.resourceId !== this.deleteTarget.resourceId)

        this.showDeleteConfirm = false
        this.deleteTarget = null
      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Failed to delete draft'
        console.error('Delete draft error:', error)
      } finally {
        this.loading = false
      }
    },

    async retrySave(draft) {
      draft.autoSaveInProgress = true
      draft.error = null

      try {
        // Reload draft data and retry save
        const payload = { ...draft.formData }
        await this.$axios.post(`/api/resources/${draft.resourceId}/draft`, payload)
        draft.error = null
      } catch (error) {
        draft.error = error.response?.data?.error || 'Auto-save failed'
      } finally {
        draft.autoSaveInProgress = false
      }
    },

    formatTime(timestamp) {
      if (!timestamp) return 'Never'
      const date = new Date(timestamp)
      const now = new Date()
      const diffMs = now - date
      const diffMins = Math.floor(diffMs / 60000)
      const diffHours = Math.floor(diffMs / 3600000)
      const diffDays = Math.floor(diffMs / 86400000)

      if (diffMins < 1) return 'Just now'
      if (diffMins < 60) return `${diffMins} minutes ago`
      if (diffHours < 24) return `${diffHours} hours ago`
      if (diffDays < 7) return `${diffDays} days ago`

      return date.toLocaleDateString()
    },

    getCategoryName(draft) {
      if (!draft.formData?.category_id) return '-'
      // Category names would need to be loaded separately, for now return ID
      return `Category ID: ${draft.formData.category_id}`
    },

    truncate(text, maxLength) {
      if (!text) return ''
      if (text.length <= maxLength) return text
      return text.substring(0, maxLength) + '...'
    },

    checkUnsavedChanges() {
      // Check if current editing session has unsaved changes
      // This would integrate with autoSave service
      return false // For now, simple check
    }
  }
}
</script>

<style scoped>
.drafts-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.subtitle {
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

.empty-state {
  text-align: center;
  padding: 80px 20px;
  background: #fafafa;
  border-radius: 8px;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

.empty-state h3 {
  font-size: 20px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
}

.empty-state p {
  color: #666;
  font-size: 14px;
  margin-bottom: 24px;
}

.drafts-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.draft-card {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 8px;
  padding: 20px;
  position: relative;
  transition: box-shadow 0.2s;
}

.draft-card:hover {
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.draft-card.has-error {
  border-color: #ffcccc;
  background: #fff5f5;
}

.draft-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.draft-title {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
}

.draft-title h3 {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
  word-break: break-word;
}

.status-badge {
  background: #f0f0f0;
  color: #666;
  padding: 4px 8px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 500;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  white-space: nowrap;
}

.draft-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;
}

.draft-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  font-size: 13px;
  color: #666;
}

.meta-item {
  display: flex;
  gap: 8px;
}

.meta-item .label {
  font-weight: 500;
  color: #999;
  min-width: 100px;
}

.meta-item .value {
  color: #666;
}

.meta-item.description .value {
  max-width: 600px;
  word-break: break-word;
}

.error-notice {
  margin-top: 12px;
  padding: 12px;
  background: #fff;
  border: 1px solid #ffcccc;
  border-radius: 4px;
  font-size: 13px;
  color: #cc0000;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.btn-retry {
  background: #cc0000;
  color: white;
  border: none;
  padding: 4px 12px;
  border-radius: 3px;
  font-size: 12px;
  cursor: pointer;
  white-space: nowrap;
}

.btn-retry:hover {
  background: #ff0000;
}

.auto-save-indicator {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #666;
}

.pulse {
  width: 8px;
  height: 8px;
  background: #0066cc;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-secondary:hover {
  background: #e0e0e0;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-danger {
  background: #fff;
  color: #cc0000;
  border: 1px solid #cc0000;
  padding: 8px 16px;
  border-radius: 4px;
  font-size: 13px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-danger:hover {
  background: #cc0000;
  color: #fff;
}

.btn-danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
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

.modal {
  background: #fff;
  padding: 24px;
  border-radius: 8px;
  max-width: 480px;
  width: 90%;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.modal h3 {
  margin: 0 0 12px 0;
  font-size: 18px;
  color: #1a1a1a;
}

.modal p {
  margin: 0 0 20px 0;
  color: #666;
  font-size: 14px;
  line-height: 1.5;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}
</style>

    