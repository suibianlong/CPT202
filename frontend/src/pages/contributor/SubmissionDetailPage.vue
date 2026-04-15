<template>
  <div class="submission-detail">
    <div v-if="loading" class="loading">Loading...</div>

    <div v-if="submitSuccess" class="alert alert-success">
      Submitted successfully. The resource is pending review.
    </div>

    <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <div v-if="resource.status === 'Rejected'" class="rejection-alert">
      <h3>遗产未通过审核</h3>
      <div class="feedback-info">
        <p><strong>Review Time:</strong> {{ formatDateTime(resource.reviewTime) }}</p>
        <p><strong>Reviewer:</strong> {{ resource.reviewerName || 'Unknown' }}</p>
      </div>
      <div class="feedback-content">
        <h4>反馈:</h4>
        <div v-if="resource.feedback" class="feedback-item">
          <strong>Reason:</strong>
          <p>{{ resource.feedback }}</p>
        </div>
        <div v-if="resource.detailedFeedback" class="feedback-item">
          <strong>Details:</strong>
          <p>{{ resource.detailedFeedback }}</p>
        </div>
        <div v-if="resource.suggestedImprovements" class="feedback-item">
          <strong>Suggestions:</strong>
          <p>{{ resource.suggestedImprovements }}</p>
        </div>
        <p v-if="!resource.feedback && !resource.detailedFeedback && !resource.suggestedImprovements" class="no-feedback">
          No feedback provided.
        </p>
      </div>
      <button class="btn-primary" @click="handleResubmit" :disabled="loading">
        Edit and Resubmit
      </button>
    </div>

    <div v-if="resource.status === 'Approved'" class="status-banner approved">
      This resource has been approved and published.
    </div>

    <div v-if="resource.status === 'Pending Review'" class="status-banner pending">
      This resource is pending review.
    </div>

    <div class="resource-content" v-if="!loading">
      <h1>{{ resource.title }}</h1>

      <div class="meta">
        <span class="badge">{{ getCategoryName(resource.categories) }}</span>
        <span v-if="resource.place" class="place">{{ resource.place }}</span>
        <span :class="['status', getStatusClass(resource.status)]">{{ resource.status }}</span>
      </div>

      <div class="section">
        <h3>描述</h3>
        <p class="text">{{ resource.description }}</p>
      </div>

      <div class="section" v-if="resource.tags && resource.tags.length">
        <h3>标签</h3>
        <div class="tags">
          <span v-for="tag in resource.tags" :key="tag.tag_id || tag.tag_name" class="tag">
            {{ tag.tag_name }}
          </span>
        </div>
      </div>

      <div class="section">
        <h3>版权声明</h3>
        <p class="text">{{ resource.copyright_declaration }}</p>
      </div>

      <div class="section" v-if="resource.usage_declaration">
        <h3>使用声明</h3>
        <p class="text">{{ resource.usage_declaration }}</p>
      </div>

      <div class="timestamps">
        <p><strong>创建时间:</strong> {{ formatDateTime(resource.created_time) }}</p>
        <p><strong>提交时间:</strong> {{ formatDateTime(resource.last_submitted_time) }}</p>
        <p><strong>更新时间:</strong> {{ formatDateTime(resource.last_updated_time) }}</p>
        <p v-if="resource.last_published_time"><strong>发布时间:</strong> {{ formatDateTime(resource.last_published_time) }}</p>
      </div>
    </div>

    <div class="actions" v-if="!loading">
      <button
        v-if="canEdit"
        class="btn-secondary"
        @click="handleEdit"
        :disabled="loading"
      >
        Edit
      </button>
      <button
        v-if="resource.status === 'Draft' || resource.status === 'Rejected'"
        class="btn-primary"
        @click="handleSubmit"
        :disabled="loading"
      >
        {{ loading ? '提交中...' : '提交审核' }}
      </button>
      <button class="btn-secondary" @click="goBack">
        返回列表
      </button>
    </div>
  </div>
</template>

<script>
import axios from 'axios'

const API_BASE = '/api/resources'

export default {
  name: 'SubmissionDetail',
  data() {
    return {
      resource: {
        resource_id: '',
        title: '',
        categories: [],
        tags: [],
        place: '',
        description: '',
        copyright_declaration: '',
        usage_declaration: '',
        status: 'Draft',
        last_submitted_time: '',
        created_time: '',
        last_updated_time: '',
        last_published_time: '',
        reviewTime: '',
        reviewerName: '',
        feedback: '',
        detailedFeedback: '',
        suggestedImprovements: ''
      },
      loading: false,
      submitSuccess: false,
      errorMessage: ''
    }
  },
  computed: {
    canEdit() {
      return ['Draft', 'Rejected'].includes(this.resource.status)
    }
  },
  methods: {
    async fetchResource() {
      this.loading = true
      this.errorMessage = ''

      try {
        const id = this.$route.params.id
        const response = await axios.get(`${API_BASE}/${id}`)
        const data = response.data

        this.resource = {
          resource_id: data.resource_id || data.id,
          title: data.title || '',
          categories: data.categories || [],
          tags: data.tags || [],
          place: data.place || '',
          description: data.description || '',
          copyright_declaration: data.copyright_declaration || '',
          usage_declaration: data.usage_declaration || '',
          status: data.status || 'Draft',
          last_submitted_time: data.last_submitted_time || '',
          created_time: data.created_time || '',
          last_updated_time: data.last_updated_time || '',
          last_published_time: data.last_published_time || '',
          reviewTime: data.reviewTime || '',
          reviewerName: data.reviewerName || '',
          feedback: data.feedback || '',
          detailedFeedback: data.detailedFeedback || '',
          suggestedImprovements: data.suggestedImprovements || ''
        }

      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Failed to load resource details'
      } finally {
        this.loading = false
      }
    },

    async handleSubmit() {
      this.loading = true
      this.errorMessage = ''
      this.submitSuccess = false

      try {
        await axios.post(`${API_BASE}/${this.resource.resource_id}/submit`)
        this.submitSuccess = true
        await this.fetchResource()

        setTimeout(() => {
          this.submitSuccess = false
        }, 3000)

      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Failed to submit for review'
      } finally {
        this.loading = false
      }
    },

    handleEdit() {
      this.$router.push(`/contributor/edit/${this.resource.resource_id}`)
    },

    handleResubmit() {
      this.$router.push(`/contributor/edit/${this.resource.resource_id}`)
    },

    goBack() {
      this.$router.push('/contributor/my-submissions')
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
    this.fetchResource()
  }
}
</script>

<style scoped>
.submission-detail {
  max-width: 720px;
  margin: 0 auto;
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

.alert-success {
  background: #e6f7e6;
  color: #22863a;
  border: 1px solid #b8e6b8;
}

.alert-error {
  background: #ffe6e6;
  color: #cc0000;
  border: 1px solid #ffcccc;
}

.status-banner {
  padding: 12px 16px;
  border-radius: 4px;
  margin-bottom: 20px;
  font-size: 14px;
}

.status-banner.approved {
  background: #e6f7e6;
  color: #22863a;
}

.status-banner.pending {
  background: #e6f0ff;
  color: #0066cc;
}

.rejection-alert {
  background: #fff5f5;
  border: 1px solid #ffcccc;
  border-radius: 6px;
  padding: 20px;
  margin-bottom: 20px;
}

.rejection-alert h3 {
  font-size: 16px;
  font-weight: 600;
  color: #cc0000;
  margin-bottom: 12px;
}

.feedback-info {
  margin-bottom: 16px;
}

.feedback-info p {
  font-size: 14px;
  color: #666;
  margin: 4px 0;
}

.feedback-content h4 {
  font-size: 14px;
  font-weight: 500;
  margin-bottom: 8px;
}

.feedback-item {
  margin-bottom: 12px;
  padding: 10px;
  background: #fff;
  border-radius: 4px;
}

.feedback-item strong {
  display: block;
  font-size: 13px;
  color: #666;
  margin-bottom: 4px;
}

.feedback-item p {
  font-size: 14px;
  color: #333;
  margin: 0;
  white-space: pre-wrap;
  line-height: 1.5;
}

.no-feedback {
  color: #999;
  font-style: italic;
  font-size: 14px;
}

.rejection-alert .btn-primary {
  margin-top: 16px;
}

.resource-content h1 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin-bottom: 12px;
}

.meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 24px;
  color: #666;
  font-size: 14px;
}

.badge {
  background: #e6f0ff;
  color: #0066cc;
  padding: 4px 8px;
  border-radius: 3px;
  font-size: 13px;
}

.place {
  color: #666;
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

.section {
  margin-bottom: 24px;
}

.section h3 {
  font-size: 14px;
  font-weight: 600;
  color: #333;
  margin-bottom: 8px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}

.text {
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

.timestamps {
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #eee;
  font-size: 13px;
  color: #666;
}

.timestamps p {
  margin: 4px 0;
}

.actions {
  margin-top: 32px;
  display: flex;
  gap: 8px;
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

.btn-secondary {
  background: #f0f0f0;
  color: #333;
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  font-size: 14px;
  cursor: pointer;
}

.btn-primary:disabled,
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
