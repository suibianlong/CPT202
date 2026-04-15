<template>
  <div class="my-submissions">
    <div class="page-header">
      <h2>My Contributions</h2>
      <button class="btn-primary" @click="createNew">Contribute Heritage</button>
    </div>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <div class="filter-bar">
      <select v-model="filterStatus" @change="fetchResources">
        <option value="">All Status</option>
        <option value="Draft">Draft</option>
        <option value="Pending Review">Pending Review</option>
        <option value="Rejected">Rejected</option>
        <option value="Approved">Approved</option>
      </select>
    </div>

    <table class="table" v-if="!loading && resources.length > 0">
      <thead>
        <tr>
          <th>Title</th>
          <th>Category</th>
          <th>Status</th>
          <th>Submitted</th>
          <th>Updated</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="resource in resources" :key="resource.resource_id || resource.id">
          <td>
            <a @click="viewDetail(resource.resource_id || resource.id)" class="title-link">
              {{ resource.title }}
            </a>
          </td>
          <td>{{ getCategoryName(resource.categories) }}</td>
          <td>
            <span :class="['status', getStatusClass(resource.status)]">
              {{ resource.status }}
            </span>
          </td>
          <td>{{ formatDateTime(resource.last_submitted_time) }}</td>
          <td>{{ formatDateTime(resource.last_updated_time) }}</td>
          <td class="actions">
            <button
              v-if="canEdit(resource.status)"
              @click="editResource(resource.resource_id || resource.id)"
              class="btn-text"
            >
              Edit
            </button>
            <button
              v-if="resource.status === 'Rejected'"
              @click="viewFeedback(resource.resource_id || resource.id)"
              class="btn-text"
            >
              Feedback
            </button>
            <button
              v-if="resource.status === 'Draft'"
              @click="confirmDelete(resource)"
              class="btn-text btn-danger"
            >
              Delete
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <div v-if="!loading && resources.length === 0" class="empty">
      No contributions yet.
      <a @click="createNew" class="link">Contribute your first heritage item</a>
    </div>

    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="cancelDelete">
      <div class="modal">
        <h3>Confirm Delete</h3>
        <p>Delete draft "{{ deleteTarget.title }}"? This cannot be undone.</p>
        <div class="modal-actions">
          <button @click="cancelDelete" class="btn-secondary">Cancel</button>
          <button @click="executeDelete" class="btn-danger" :disabled="deleting">
            {{ deleting ? 'Deleting...' : 'Delete' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

const API_BASE = '/api/resources'

export default {
  name: 'MySubmissions',
  data() {
    return {
      filterStatus: '',
      resources: [],
      loading: false,
      errorMessage: '',
      showDeleteConfirm: false,
      deleteTarget: null,
      deleting: false
    }
  },
  methods: {
    async fetchResources() {
      this.loading = true
      this.errorMessage = ''

      try {
        const params = {}
        if (this.filterStatus) {
          params.status = this.filterStatus
        }
        const response = await axios.get(`${API_BASE}/my`, { params })
        this.resources = response.data || []
      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Failed to load submissions'
      } finally {
        this.loading = false
      }
    },

    createNew() {
      this.$router.push('/contributor/create')
    },

    viewDetail(id) {
      this.$router.push(`/contributor/detail/${id}`)
    },

    editResource(id) {
      this.$router.push(`/contributor/edit/${id}`)
    },

    viewFeedback(id) {
      this.$router.push(`/contributor/detail/${id}`)
    },

    confirmDelete(resource) {
      this.deleteTarget = resource
      this.showDeleteConfirm = true
    },

    cancelDelete() {
      this.showDeleteConfirm = false
      this.deleteTarget = null
    },

    async executeDelete() {
      if (!this.deleteTarget) return
      this.deleting = true

      try {
        const id = this.deleteTarget.resource_id || this.deleteTarget.id
        await axios.delete(`${API_BASE}/${id}`)
        this.resources = this.resources.filter(r => (r.resource_id || r.id) !== id)
        this.showDeleteConfirm = false
        this.deleteTarget = null
      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Failed to delete'
      } finally {
        this.deleting = false
      }
    },

    canEdit(status) {
      return ['Draft', 'Rejected'].includes(status)
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

    getCategoryName(categories) {
      if (!categories || categories.length === 0) return '-'
      return categories.map(c => c.category_topic || c.category_name).join(', ')
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
    }
  },
  mounted() {
    this.fetchResources()
  }
}
</script>

<style scoped>
.my-submissions {
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

.filter-bar {
  margin-bottom: 16px;
}

.filter-bar select {
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  background: #fff;
}

.table {
  width: 100%;
  border-collapse: collapse;
  background: #fff;
  border: 1px solid #eee;
}

.table th,
.table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #eee;
  font-size: 14px;
}

.table th {
  background: #fafafa;
  font-weight: 500;
  color: #666;
  font-size: 12px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.table tbody tr:last-child td {
  border-bottom: none;
}

.table tbody tr:hover {
  background: #fafafa;
}

.title-link {
  color: #0066cc;
  cursor: pointer;
  text-decoration: none;
  font-weight: 500;
}

.title-link:hover {
  text-decoration: underline;
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

.actions {
  text-align: right;
}

.btn-text {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 13px;
  padding: 4px 8px;
  font-weight: 500;
}

.btn-text:hover {
  text-decoration: underline;
}

.btn-text.btn-danger {
  color: #eff1f3;
}

.empty {
  text-align: center;
  padding: 64px 24px;
  color: #999;
  background: #fff;
  border: 1px solid #eee;
  font-size: 14px;
}

.link {
  color: #0066cc;
  cursor: pointer;
  text-decoration: none;
  margin-left: 8px;
}

.link:hover {
  text-decoration: underline;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal {
  background: #fff;
  padding: 24px;
  border-radius: 6px;
  max-width: 400px;
  width: 90%;
}

.modal h3 {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 12px;
}

.modal p {
  font-size: 14px;
  color: #666;
  margin-bottom: 20px;
}

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.btn-primary {
  background: #1a1a1a;
  color: #fff;
  padding: 8px 16px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-primary:hover {
  background: #333;
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

.btn-danger {
  background: #cc0000;
  color: #fff;
  padding: 8px 16px;
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
