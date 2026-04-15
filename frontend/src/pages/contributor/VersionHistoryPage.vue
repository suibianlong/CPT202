<template>
  <div class="version-history">
    <div class="page-header">
      <h2>版本历史</h2>
      <button class="btn-secondary" @click="goBack">Back</button>
    </div>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <div v-if="!loading && versions.length === 0" class="empty">
      <p>No version history available.</p>
      <button class="btn-secondary" @click="goBack">Back</button>
    </div>

    <div v-if="!loading && versions.length > 0" class="content">
      <!-- Resource Summary -->
      <div class="resource-summary">
        <h3>{{ resourceTitle }}</h3>
        <p class="subtitle">Resource ID: {{ resourceId }}</p>
      </div>

      <!-- Version List -->
      <table class="version-table">
        <thead>
          <tr>
            <th>Version</th>
            <th>Status</th>
            <th>Submitted By</th>
            <th>Submitted At</th>
            <th>Changes</th>
            <th>Review</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="version in versions" :key="version.versionNo" :class="{ 'current-version': version.versionNo === currentVersion }">
            <td>
              <span class="version-badge">v{{ version.versionNo }}</span>
              <span v-if="version.isLatest" class="latest-badge">Latest</span>
            </td>
            <td>
              <span :class="['status', getStatusClass(version.status)]">
                {{ version.status }}
              </span>
            </td>
            <td>{{ version.submittedBy }}</td>
            <td>{{ formatDateTime(version.submittedAt) }}</td>
            <td>{{ version.fieldCount || 'N/A' }} fields changed</td>
            <td>
              <div v-if="version.review" class="review-summary">
                <span :class="['action', version.review.status.toLowerCase()]">
                  {{ version.review.status }}
                </span>
                <small v-if="version.review.reviewerName">by {{ version.review.reviewerName }}</small>
              </div>
              <span v-else class="no-review">Not reviewed</span>
            </td>
            <td class="actions">
              <button class="btn-text" @click="viewVersion(version.versionNo)" :disabled="version.versionNo === currentVersion">
                View
              </button>
              <button class="btn-text" @click="compareVersions(currentVersion, version.versionNo)" v-if="version.versionNo !== currentVersion">
                Compare
              </button>
              <button class="btn-text btn-success" @click="rollbackVersion(version.versionNo)" v-if="version.versionNo !== currentVersion && canRollback">
                Rollback
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  </div>
</template>

<script>
import { getVersionHistory, compareVersions, getVersionDetail, rollbackVersion } from '../api/versionApi'

export default {
  name: 'VersionHistoryPage',
  data() {
    return {
      resourceId: null,
      resourceTitle: '',
      versions: [],
      currentVersion: null,
      loading: false,
      errorMessage: ''
    }
  },
  computed: {
    canRollback() {
      return this.currentVersion > 1
    }
  },
  methods: {
    async fetchVersions() {
      this.loading = true
      this.errorMessage = ''

      try {
        const resourceId = this.$route.params.id
        this.resourceId = resourceId

        const history = await getVersionHistory(resourceId)
        this.versions = history.versions || []
        this.currentVersion = history.currentVersion || 1
        this.resourceTitle = history.resourceTitle || 'Resource'
      } catch (error) {
        this.errorMessage = error.message || 'Failed to load version history'
      } finally {
        this.loading = false
      }
    },

    formatDateTime(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString('en-US', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    getStatusClass(status) {
      const map = {
        'Draft': 'status-draft',
        'Pending Review': 'status-pending',
        'Rejected': 'status-rejected',
        'Approved': 'status-approved'
      }
      return map[status] || ''
    },

    viewVersion(versionNo) {
      this.$router.push(`/contributor/resource/${this.resourceId}/version/${versionNo}`)
    },

    compareVersions(v1, v2) {
      this.$router.push(`/contributor/resource/${this.resourceId}/compare?v1=${v1}&v2=${v2}`)
    },

    async rollbackVersion(versionNo) {
      if (!confirm(`Are you sure you want to rollback to version ${versionNo}?`)) {
        return
      }

      this.loading = true
      try {
        await rollbackVersion(this.resourceId, versionNo)
        await this.fetchVersions()
        alert(`Successfully rolled back to version ${versionNo}`)
      } catch (error) {
        alert('Failed to rollback: ' + (error.message || 'Unknown error'))
      } finally {
        this.loading = false
      }
    },

    goBack() {
      this.$router.push(`/contributor/detail/${this.resourceId}`)
    }
  },

  mounted() {
    this.fetchVersions()
  }
}
</script>

<style scoped>
.version-history {
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.loading {
  text-align: center;
  padding: 48px;
  color: #666;
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

.empty {
  text-align: center;
  padding: 64px 24px;
  color: #999;
  background: #fff;
  border: 1px solid #eee;
  font-size: 14px;
}

.content {
  margin-top: 24px;
}

.resource-summary {
  background: #fff;
  padding: 16px;
  border: 1px solid #eee;
  border-bottom: none;
  border-radius: 6px 6px 0 0;
}

.resource-summary h3 {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 4px 0;
}

.resource-summary .subtitle {
  font-size: 13px;
  color: #999;
  margin: 0;
}

.version-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 0 0 6px 6px;
  overflow: hidden;
}

.version-table th,
.version-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}

.version-table th {
  background: #fafafa;
  font-weight: 500;
  color: #666;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.version-table tbody tr:last-child td {
  border-bottom: none;
}

.version-table tbody tr:hover {
  background: #fafafa;
}

.version-table tbody tr.current-version {
  background: #f0f9ff;
}

.version-badge {
  display: inline-block;
  background: #e6f0ff;
  color: #0066cc;
  padding: 4px 8px;
  border-radius: 3px;
  font-size: 13px;
  font-weight: 500;
}

.latest-badge {
  display: inline-block;
  background: #e6f7e6;
  color: #22863a;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 11px;
  font-weight: 500;
  margin-left: 8px;
}

.status {
  padding: 4px 8px;
  border-radius: 3px;
  font-size: 12px;
  font-weight: 500;
}

.status-draft { background: #f0f0f0; color: #666; }
.status-pending { background: #e6f0ff; color: #0066cc; }
.status-rejected { background: #ffe6e6; color: #cc0000; }
.status-approved { background: #e6f7e6; color: #22863a; }

.review-summary {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.review-summary .action {
  font-size: 12px;
  font-weight: 500;
  padding: 2px 6px;
  border-radius: 2px;
  display: inline-block;
  width: fit-content;
}

.review-summary .action.approve { background: #e6f7e6; color: #22863a; }
.review-summary .action.reject { background: #ffe6e6; color: #cc0000; }

.review-summary small {
  color: #999;
  font-size: 11px;
}

.no-review {
  color: #999;
  font-size: 12px;
  font-style: italic;
}

.actions {
  text-align: right;
}

.btn-text {
  background: none;
  border: none;
  color: #0066cc;
  cursor: pointer;
  font-size: 13px;
  padding: 4px 8px;
}

.btn-text:hover {
  text-decoration: underline;
}

.btn-text:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-text.btn-danger {
  color: #cc0000;
}

.btn-text.btn-success {
  color: #22863a;
}

.btn-secondary {
  background: #f0f0f0;
  color: #333;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-secondary:hover {
  background: #ddd;
}
</style>
