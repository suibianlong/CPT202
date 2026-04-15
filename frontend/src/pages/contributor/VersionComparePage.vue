<template>
  <div class="version-compare">
    <div class="page-header">
      <h2>版本对比</h2>
      <div class="header-actions">
        <select v-model="version1" class="version-select" @change="loadComparison">
          <option v-for="v in versions" :key="v.versionNo" :value="v.versionNo">
            Version {{ v.versionNo }}
          </option>
        </select>
        <span class="vs">vs</span>
        <select v-model="version2" class="version-select" @change="loadComparison">
          <option v-for="v in versions" :key="v.versionNo" :value="v.versionNo">
            Version {{ v.versionNo }}
          </option>
        </select>
        <button class="btn-secondary" @click="goBack">Back</button>
      </div>
    </div>

    <div v-if="loading" class="loading">Loading comparison...</div>

    <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <div v-if="!loading && comparison" class="comparison-content">
      <div class="version-labels">
        <div class="version-label old">
          <span class="label-badge">v{{ version1 }}</span>
          <span class="label-date">{{ formatDateTime(comparison.version1?.submittedAt) }}</span>
        </div>
        <div class="version-label new">
          <span class="label-badge">v{{ version2 }}</span>
          <span class="label-date">{{ formatDateTime(comparison.version2?.submittedAt) }}</span>
        </div>
      </div>

      <!-- Field-by-field comparison -->
      <table class="diff-table">
        <thead>
          <tr>
            <th>Field</th>
            <th>Version {{ version1 }}</th>
            <th>Version {{ version2 }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="field in comparison.fields" :key="field.name" :class="field.changeType">
            <td class="field-name">{{ field.label }}</td>
            <td class="field-value old-value" v-html="formatValue(field.oldValue)"></td>
            <td class="field-value new-value" v-html="formatValue(field.newValue)"></td>
          </tr>
        </tbody>
      </table>

      <!-- Summary -->
      <div class="diff-summary">
        <h3>Change Summary</h3>
        <div class="summary-stats">
          <span class="stat added">
            <span class="stat-count">{{ comparison.stats?.added || 0 }}</span>
            <span class="stat-label">Added</span>
          </span>
          <span class="stat removed">
            <span class="stat-count">{{ comparison.stats?.removed || 0 }}</span>
            <span class="stat-label">Removed</span>
          </span>
          <span class="stat modified">
            <span class="stat-count">{{ comparison.stats?.modified || 0 }}</span>
            <span class="stat-label">Modified</span>
          </span>
          <span class="stat unchanged">
            <span class="stat-count">{{ comparison.stats?.unchanged || 0 }}</span>
            <span class="stat-label">Unchanged</span>
          </span>
        </div>
      </div>

      <div class="actions">
        <button class="btn-secondary" @click="goBack">Back</button>
        <button class="btn-secondary" @click="viewVersion1">View v{{ version1 }} Full</button>
        <button class="btn-secondary" @click="viewVersion2">View v{{ version2 }} Full</button>
      </div>
    </div>
  </div>
</template>

<script>
import { compareVersions, getVersionHistory } from '../api/versionApi'

export default {
  name: 'VersionComparePage',
  data() {
    return {
      resourceId: null,
      versions: [],
      version1: null,
      version2: null,
      comparison: null,
      loading: false,
      errorMessage: ''
    }
  },
  methods: {
    async loadVersions() {
      try {
        const resourceId = this.$route.params.id
        this.resourceId = resourceId
        const history = await getVersionHistory(resourceId)
        this.versions = history.versions || []

        // Set default versions from query params or latest two
        const v1 = parseInt(this.$route.query.v1)
        const v2 = parseInt(this.$route.query.v2)

        if (v1 && v2) {
          this.version1 = v1
          this.version2 = v2
        } else if (this.versions.length >= 2) {
          this.version1 = this.versions[1].versionNo
          this.version2 = this.versions[0].versionNo
        } else if (this.versions.length === 1) {
          this.version1 = this.version2 = this.versions[0].versionNo
        }

        if (this.version1 && this.version2) {
          await this.loadComparison()
        }
      } catch (error) {
        this.errorMessage = error.message || 'Failed to load versions'
      }
    },

    async loadComparison() {
      if (!this.version1 || !this.version2) return

      this.loading = true
      this.errorMessage = ''

      try {
        this.comparison = await compareVersions(this.resourceId, this.version1, this.version2)
      } catch (error) {
        this.errorMessage = error.message || 'Failed to load comparison'
      } finally {
        this.loading = false
      }
    },

    formatDateTime(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString('en-US', {
        month: 'short',
        day: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      })
    },

    formatValue(value) {
      if (value === null || value === undefined || value === '') {
        return '<span class="empty">(empty)</span>'
      }
      // Escape HTML
      const escaped = String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\n/g, '<br>')
      return escaped
    },

    viewVersion1() {
      this.$router.push(`/contributor/resource/${this.resourceId}/version/${this.version1}`)
    },

    viewVersion2() {
      this.$router.push(`/contributor/resource/${this.resourceId}/version/${this.version2}`)
    },

    goBack() {
      this.$router.push(`/contributor/resource/${this.resourceId}/versions`)
    }
  },

  mounted() {
    this.loadVersions()
  }
}
</script>

<style scoped>
.version-compare {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.version-select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: #fff;
}

.vs {
  color: #999;
  font-weight: 500;
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

.comparison-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.version-labels {
  display: flex;
  justify-content: space-around;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 16px;
}

.version-label {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.label-badge {
  font-size: 16px;
  font-weight: 600;
}

.version-label.old .label-badge {
  color: #cc0000;
}

.version-label.new .label-badge {
  color: #22863a;
}

.label-date {
  font-size: 12px;
  color: #999;
}

.diff-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
}

.diff-table th,
.diff-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 14px;
  vertical-align: top;
}

.diff-table th {
  background: #fafafa;
  font-weight: 500;
  color: #666;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  width: 120px;
}

.diff-table tbody tr:last-child td {
  border-bottom: none;
}

.diff-table tbody tr.added {
  background: #f0fff0;
}

.diff-table tbody tr.added td.new-value {
  color: #22863a;
  font-weight: 500;
}

.diff-table tbody tr.removed {
  background: #fff5f5;
}

.diff-table tbody tr.removed td.old-value {
  color: #cc0000;
  font-weight: 500;
  text-decoration: line-through;
}

.diff-table tbody tr.modified {
  background: #fffbeb;
}

.diff-table tbody tr.modified td.new-value {
  color: #b45309;
}

.field-name {
  font-weight: 500;
  color: #666;
  background: #fafafa;
}

.field-value {
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-word;
}

.field-value .empty {
  color: #999;
  font-style: italic;
}

.diff-summary {
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 20px;
}

.diff-summary h3 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin: 0 0 16px 0;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.summary-stats {
  display: flex;
  gap: 32px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-count {
  font-size: 24px;
  font-weight: 600;
}

.stat.added .stat-count { color: #22863a; }
.stat.removed .stat-count { color: #cc0000; }
.stat.modified .stat-count { color: #b45309; }
.stat.unchanged .stat-count { color: #666; }

.stat-label {
  font-size: 12px;
  color: #999;
  text-transform: uppercase;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
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
