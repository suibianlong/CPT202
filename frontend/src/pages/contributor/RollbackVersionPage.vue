<template>
  <div class="rollback-page">
    <div class="page-header">
      <h2>回滚到版本 {{ versionNo }}</h2>
      <button class="btn-secondary" @click="goBack">Cancel</button>
    </div>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <div v-if="!loading && conflict" class="conflict-content">
      <div class="warning-banner">
        <strong>Conflict Detected</strong>
        <p>Some fields have been modified since version {{ versionNo }}. Please select which version to keep for each conflicting field.</p>
      </div>

      <table class="conflict-table">
        <thead>
          <tr>
            <th>Field</th>
            <th>Version {{ versionNo }} (Target)</th>
            <th>Current Version</th>
            <th>Select</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="field in conflict.fields" :key="field.name" :class="{ conflict: field.hasConflict }">
            <td class="field-name">{{ field.label }}</td>
            <td class="field-value target-value" v-html="formatValue(field.targetValue)"></td>
            <td class="field-value current-value" v-html="formatValue(field.currentValue)"></td>
            <td class="selection">
              <div v-if="field.hasConflict" class="radio-group">
                <label>
                  <input type="radio" :name="'field_' + field.name" value="target" v-model="field.selected" />
                  Keep v{{ versionNo }}
                </label>
                <label>
                  <input type="radio" :name="'field_' + field.name" value="current" v-model="field.selected" />
                  Keep Current
                </label>
              </div>
              <span v-else class="no-conflict">No conflict</span>
            </td>
          </tr>
        </tbody>
      </table>

      <div class="actions">
        <button class="btn-secondary" @click="goBack">Cancel</button>
        <button class="btn-primary" @click="confirmRollback" :disabled="!canConfirm || processing">
          {{ processing ? 'Rolling back...' : 'Confirm Rollback' }}
        </button>
      </div>
    </div>

    <div v-if="!loading && !conflict && rollbackData" class="rollback-content">
      <div class="confirm-section">
        <h3>Confirm Rollback</h3>
        <p>You are about to rollback to version {{ versionNo }}.</p>
        <p>This will restore the following data:</p>

        <div class="rollback-preview">
          <div class="preview-item" v-for="field in rollbackData.fields" :key="field.name">
            <span class="preview-label">{{ field.label }}:</span>
            <span class="preview-value" v-html="formatValue(field.value)"></span>
          </div>
        </div>

        <div class="actions">
          <button class="btn-secondary" @click="goBack">Cancel</button>
          <button class="btn-danger" @click="confirmRollback" :disabled="processing">
            {{ processing ? 'Rolling back...' : 'Confirm Rollback' }}
          </button>
        </div>
      </div>
    </div>

    <div v-if="!loading && success" class="success-message">
      <div class="success-icon">&#10003;</div>
      <h3>Rollback Successful</h3>
      <p>Resource has been rolled back to version {{ versionNo }}.</p>
      <button class="btn-primary" @click="goToDetail">View Resource</button>
    </div>
  </div>
</template>

<script>
import { getVersionDetail, rollbackVersion, getVersionConflict } from '../api/versionApi'

export default {
  name: 'RollbackVersionPage',
  data() {
    return {
      resourceId: null,
      versionNo: null,
      conflict: null,
      rollbackData: null,
      loading: false,
      processing: false,
      errorMessage: '',
      success: false
    }
  },
  computed: {
    canConfirm() {
      if (!this.conflict) return true
      return this.conflict.fields
        .filter(f => f.hasConflict)
        .every(f => f.selected)
    }
  },
  methods: {
    async loadData() {
      this.loading = true
      this.errorMessage = ''

      try {
        const resourceId = this.$route.params.id
        const versionNo = parseInt(this.$route.params.versionNo)
        this.resourceId = resourceId
        this.versionNo = versionNo

        // Check for conflicts
        try {
          this.conflict = await getVersionConflict(resourceId, versionNo)
        } catch (e) {
          this.conflict = null
        }

        // Get rollback data
        this.rollbackData = await getVersionDetail(resourceId, versionNo)

      } catch (error) {
        this.errorMessage = error.message || 'Failed to load rollback data'
      } finally {
        this.loading = false
      }
    },

    formatValue(value) {
      if (value === null || value === undefined || value === '') {
        return '<span class="empty">(empty)</span>'
      }
      const escaped = String(value)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/\n/g, '<br>')
      return escaped
    },

    async confirmRollback() {
      this.processing = true
      this.errorMessage = ''

      try {
        // Build selected values from conflict resolution
        let selectedValues = null
        if (this.conflict) {
          selectedValues = {}
          for (const field of this.conflict.fields) {
            if (field.hasConflict) {
              selectedValues[field.name] = field.selected === 'target' ? field.targetValue : field.currentValue
            }
          }
        }

        await rollbackVersion(this.resourceId, this.versionNo, selectedValues)
        this.success = true
      } catch (error) {
        this.errorMessage = error.message || 'Failed to rollback'
      } finally {
        this.processing = false
      }
    },

    goBack() {
      this.$router.push(`/contributor/resource/${this.resourceId}/versions`)
    },

    goToDetail() {
      this.$router.push(`/contributor/detail/${this.resourceId}`)
    }
  },

  mounted() {
    this.loadData()
  }
}
</script>

<style scoped>
.rollback-page {
  max-width: 900px;
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

.warning-banner {
  background: #fff3e0;
  border: 1px solid #ffcc80;
  border-radius: 6px;
  padding: 16px;
  margin-bottom: 20px;
}

.warning-banner strong {
  color: #e65100;
  display: block;
  margin-bottom: 4px;
}

.warning-banner p {
  color: #bf360c;
  margin: 0;
  font-size: 14px;
}

.conflict-table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  overflow: hidden;
  margin-bottom: 24px;
}

.conflict-table th,
.conflict-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}

.conflict-table th {
  background: #fafafa;
  font-weight: 500;
  color: #666;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.conflict-table tbody tr:last-child td {
  border-bottom: none;
}

.conflict-table tbody tr.conflict {
  background: #fff8e1;
}

.field-name {
  font-weight: 500;
  color: #333;
}

.field-value {
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-word;
  max-width: 250px;
}

.target-value {
  background: #e3f2fd;
  border-radius: 4px;
  padding: 8px;
}

.current-value {
  background: #fff3e0;
  border-radius: 4px;
  padding: 8px;
}

.selection {
  text-align: center;
}

.radio-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 12px;
}

.radio-group label {
  display: flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
}

.radio-group input[type="radio"] {
  margin: 0;
}

.no-conflict {
  color: #999;
  font-style: italic;
  font-size: 12px;
}

.conflict-section h3,
.confirm-section h3 {
  font-size: 16px;
  font-weight: 600;
  color: #333;
  margin-bottom: 12px;
}

.rollback-preview {
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 6px;
  padding: 16px;
  margin: 16px 0;
}

.preview-item {
  display: flex;
  margin-bottom: 12px;
}

.preview-item:last-child {
  margin-bottom: 0;
}

.preview-label {
  font-weight: 500;
  color: #666;
  width: 120px;
  flex-shrink: 0;
}

.preview-value {
  color: #333;
  font-family: monospace;
  word-break: break-word;
}

.actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 24px;
}

.success-message {
  text-align: center;
  padding: 48px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
}

.success-icon {
  width: 64px;
  height: 64px;
  background: #e6f7e6;
  color: #22863a;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin: 0 auto 16px;
}

.success-message h3 {
  font-size: 18px;
  font-weight: 600;
  color: #22863a;
  margin-bottom: 8px;
}

.success-message p {
  color: #666;
  margin-bottom: 24px;
}

.btn-primary {
  background: #1a1a1a;
  color: #fff;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #333;
}

.btn-primary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
  background: #ddd;
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

.btn-danger:hover {
  background: #aa0000;
}

.btn-danger:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
