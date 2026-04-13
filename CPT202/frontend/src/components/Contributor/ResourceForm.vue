<template>
  <div class="resource-form">
    <div class="page-header">
      <h2>{{ isEdit ? 'Edit Resource' : 'Create Resource' }}</h2>
    </div>

    <div v-if="submitSuccess" class="alert alert-success">
      Submitted successfully. The resource is pending review.
    </div>

    <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

    <form @submit.prevent="handleSubmit" :disabled="isReadOnly || loading">
      <div class="form-group">
        <label for="title">Title *</label>
        <input
          id="title"
          v-model="formData.title"
          type="text"
          required
          :disabled="isReadOnly"
          placeholder="Enter resource title"
          maxlength="255"
        />
      </div>

      <div class="form-group">
        <label for="category">Category *</label>
        <select id="category" v-model="formData.category_id" required :disabled="isReadOnly">
          <option value="">Select category</option>
          <option v-for="cat in categories" :key="cat.category_id" :value="cat.category_id">
            {{ cat.category_topic || cat.category_name }}
          </option>
        </select>
      </div>

      <div class="form-group">
        <label for="place">Location</label>
        <input
          id="place"
          v-model="formData.place"
          type="text"
          :disabled="isReadOnly"
          placeholder="Enter related location"
          maxlength="255"
        />
      </div>

      <div class="form-group">
        <label for="description">Description *</label>
        <textarea
          id="description"
          v-model="formData.description"
          rows="5"
          required
          :disabled="isReadOnly"
          placeholder="Describe this resource in detail"
        ></textarea>
      </div>

      <div class="form-group">
        <label for="tags">Tags</label>
        <input
          id="tags"
          v-model="tagsInput"
          type="text"
          :disabled="isReadOnly"
          placeholder="Separate multiple tags with commas"
        />
        <small>Enter tag names separated by commas</small>
      </div>

      <div class="form-group">
        <label>Files * (min 1 file required)</label>
        <div class="file-upload" v-if="!isReadOnly">
          <input
            type="file"
            ref="fileInput"
            @change="handleFileSelect"
            :disabled="loading"
            accept=".jpg,.jpeg,.png,.gif,.webp,.pdf,.doc,.docx,.xls,.xlsx,.txt,.mp4,.mpeg"
          />
          <button
            type="button"
            class="btn-secondary"
            @click="triggerFileSelect"
            :disabled="loading"
          >
            Select File
          </button>
          <span v-if="selectedFile" class="selected-file">{{ selectedFile.name }}</span>
          <button
            v-if="selectedFile"
            type="button"
            class="btn-primary"
            @click="uploadFile"
            :disabled="uploading"
          >
            {{ uploading ? 'Uploading...' : 'Upload' }}
          </button>
        </div>

        <div class="file-list" v-if="uploadedFiles.length > 0">
          <div v-for="file in uploadedFiles" :key="file.file_id" class="file-item">
            <div class="file-info">
              <span class="file-name" :title="file.original_filename">
                {{ file.original_filename }}
              </span>
              <span class="file-size">({{ formatFileSize(file.file_size) }})</span>
            </div>
            <button
              v-if="!isReadOnly"
              type="button"
              class="btn-icon"
              @click="deleteFile(file.file_id)"
              :disabled="deletingFile === file.file_id"
            >
              x
            </button>
          </div>
        </div>

        <div v-if="uploadedFiles.length === 0 && !isReadOnly" class="file-hint">
          <small>No files uploaded. At least 1 file is required to submit.</small>
        </div>
      </div>

      <div class="form-group">
        <label for="copyright_declaration">Copyright Declaration *</label>
        <textarea
          id="copyright_declaration"
          v-model="formData.copyright_declaration"
          rows="2"
          required
          :disabled="isReadOnly"
          placeholder="Declare the copyright and license"
        ></textarea>
      </div>

      <div class="form-group">
        <label for="usage_declaration">Usage Declaration</label>
        <textarea
          id="usage_declaration"
          v-model="formData.usage_declaration"
          rows="2"
          :disabled="isReadOnly"
          placeholder="Declare how this resource can be used"
        ></textarea>
      </div>

      <div class="form-actions">
        <button
          type="button"
          class="btn-secondary"
          @click="saveDraft"
          :disabled="loading"
          v-if="!isReadOnly"
        >
          Save Draft
        </button>
        <button
          type="submit"
          class="btn-primary"
          :disabled="loading || uploadedFiles.length === 0"
          v-if="!isReadOnly"
        >
          {{ loading ? 'Submitting...' : 'Submit for Review' }}
        </button>
        <button type="button" class="btn-secondary" @click="goBack">
          Back
        </button>
      </div>
    </form>
  </div>
</template>

<script>
import axios from 'axios'

const API_BASE = '/api/resources'

export default {
  name: 'ResourceForm',
  data() {
    return {
      resourceId: null,
      isEdit: false,
      isReadOnly: false,
      hasUnsavedChanges: false,
      loading: false,
      submitSuccess: false,
      errorMessage: '',
      requireFiles: false,
      originalData: null,
      categories: [],
      tagsInput: '',
      selectedFile: null,
      uploadedFiles: [],
      uploading: false,
      deletingFile: null,
      formData: {
        title: '',
        category_id: '',
        place: '',
        description: '',
        copyright_declaration: '',
        usage_declaration: '',
        tag_names: []
      }
    }
  },
  async created() {
    await this.fetchCategories()
  },
  watch: {
    formData: {
      handler(newVal) {
        if (this.originalData) {
          this.hasUnsavedChanges = JSON.stringify(newVal) !== JSON.stringify(this.originalData)
        }
      },
      deep: true
    },
    tagsInput(newVal) {
      this.formData.tag_names = newVal
        ? newVal.split(',').map(t => t.trim()).filter(t => t)
        : []
    }
  },
  methods: {
    async fetchCategories() {
      try {
        const response = await axios.get(`${API_BASE}/categories`)
        this.categories = response.data || []
      } catch (error) {
        console.error('Fetch categories failed:', error)
      }
    },

    triggerFileSelect() {
      this.$refs.fileInput.click()
    },

    handleFileSelect(event) {
      const file = event.target.files[0]
      if (file) {
        this.selectedFile = file
        this.errorMessage = ''
        this.requireFiles = false
      }
    },

    async uploadFile() {
      if (!this.selectedFile) return

      if (!this.resourceId) {
        this.errorMessage = 'Please save as draft first before uploading files'
        return
      }

      this.uploading = true
      this.errorMessage = ''

      try {
        const formData = new FormData()
        formData.append('file', this.selectedFile)

        const response = await axios.post(
          `${API_BASE}/${this.resourceId}/upload`,
          formData,
          {
            headers: {
              'Content-Type': 'multipart/form-data'
            }
          }
        )

        this.uploadedFiles.unshift(response.data.file)
        this.selectedFile = null
        this.$refs.fileInput.value = ''

      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Failed to upload file'
      } finally {
        this.uploading = false
      }
    },

    async deleteFile(fileId) {
      if (!this.resourceId) return
      this.deletingFile = fileId
      this.errorMessage = ''

      try {
        await axios.delete(`${API_BASE}/${this.resourceId}/files/${fileId}`)
        this.uploadedFiles = this.uploadedFiles.filter(f => f.file_id !== fileId)
      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Failed to delete file'
      } finally {
        this.deletingFile = null
      }
    },

    async fetchFiles() {
      if (!this.resourceId) return
      try {
        const response = await axios.get(`${API_BASE}/${this.resourceId}/files`)
        this.uploadedFiles = response.data || []
      } catch (error) {
        console.error('Fetch files failed:', error)
      }
    },

    formatFileSize(bytes) {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },

    async saveDraft() {
      this.loading = true
      this.errorMessage = ''
      this.requireFiles = false

      try {
        const payload = this.buildPayload()

        if (this.isEdit && this.resourceId) {
          await axios.post(`${API_BASE}/${this.resourceId}/draft`, payload)
        } else {
          const response = await axios.post(API_BASE, payload)
          this.resourceId = response.data.resource_id || response.data.id
          this.isEdit = true
        }

        this.originalData = { ...this.formData }
        this.hasUnsavedChanges = false
        this.$emit('refresh-list')
        alert('Draft saved successfully')
      } catch (error) {
        this.errorMessage = error.response?.data?.error || 'Failed to save draft'
      } finally {
        this.loading = false
      }
    },

    async handleSubmit() {
      this.loading = true
      this.errorMessage = ''
      this.requireFiles = false
      this.submitSuccess = false

      try {
        if (this.uploadedFiles.length === 0) {
          this.requireFiles = true
          this.errorMessage = 'Please upload at least 1 file before submitting.'
          this.loading = false
          return
        }

        if (this.hasUnsavedChanges) {
          await this.autoSaveDraft()
        }

        if (!this.resourceId) {
          const payload = this.buildPayload()
          const response = await axios.post(API_BASE, payload)
          this.resourceId = response.data.resource_id || response.data.id
        }

        await axios.post(`${API_BASE}/${this.resourceId}/submit`)

        this.submitSuccess = true
        this.isReadOnly = true
        this.hasUnsavedChanges = false

        await this.reloadResource()
        this.$emit('refresh-list')

        setTimeout(() => {
          this.submitSuccess = false
        }, 3000)

      } catch (error) {
        const errorData = error.response?.data
        if (errorData?.require_files) {
          this.requireFiles = true
          this.errorMessage = errorData.error
        } else {
          this.errorMessage = errorData?.error || 'Failed to submit for review'
        }
      } finally {
        this.loading = false
      }
    },

    async autoSaveDraft() {
      const payload = this.buildPayload()

      if (this.resourceId) {
        await axios.post(`${API_BASE}/${this.resourceId}/draft`, payload)
      } else {
        const response = await axios.post(API_BASE, payload)
        this.resourceId = response.data.resource_id || response.data.id
      }

      this.originalData = { ...this.formData }
      this.hasUnsavedChanges = false
    },

    async reloadResource() {
      try {
        const response = await axios.get(`${API_BASE}/${this.resourceId}`)
        const resource = response.data

        this.formData = {
          title: resource.title || '',
          category_id: resource.categories?.[0]?.category_id || '',
          place: resource.place || '',
          description: resource.description || '',
          copyright_declaration: resource.copyright_declaration || '',
          usage_declaration: resource.usage_declaration || '',
          tag_names: resource.tags?.map(t => t.tag_name) || []
        }

        this.tagsInput = this.formData.tag_names.join(', ')
        this.originalData = { ...this.formData }
        this.isReadOnly = resource.status === 'Pending Review'

      } catch (error) {
        console.error('Reload resource failed:', error)
      }
    },

    buildPayload() {
      const tagNames = typeof this.tagsInput === 'string'
        ? this.tagsInput.split(',').map(t => t.trim()).filter(t => t)
        : this.formData.tag_names

      return {
        title: this.formData.title,
        category_ids: this.formData.category_id ? [this.formData.category_id] : [],
        place: this.formData.place,
        description: this.formData.description,
        copyright_declaration: this.formData.copyright_declaration,
        usage_declaration: this.formData.usage_declaration,
        tag_names: tagNames
      }
    },

    goBack() {
      this.$router.push('/contributor/my-submissions')
    },

    async initForm(resourceId) {
      this.loading = true
      this.resourceId = resourceId

      try {
        const response = await axios.get(`${API_BASE}/${resourceId}`)
        const resource = response.data

        this.isEdit = true
        this.isReadOnly = resource.status === 'Pending Review'

        const tagNames = resource.tags?.map(t => t.tag_name) || []

        this.formData = {
          title: resource.title || '',
          category_id: resource.categories?.[0]?.category_id || '',
          place: resource.place || '',
          description: resource.description || '',
          copyright_declaration: resource.copyright_declaration || '',
          usage_declaration: resource.usage_declaration || '',
          tag_names: tagNames
        }

        this.tagsInput = tagNames.join(', ')
        this.originalData = { ...this.formData }

        await this.fetchFiles()

      } catch (error) {
        this.errorMessage = 'Failed to load resource'
      } finally {
        this.loading = false
      }
    }
  },
  mounted() {
    const resourceId = this.$route.params.id
    if (resourceId) {
      this.initForm(resourceId)
    }
  }
}
</script>

<style scoped>
.resource-form {
  max-width: 640px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-header h2 {
  font-size: 20px;
  font-weight: 600;
  color: #1a1a1a;
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

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 6px;
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.form-group input[type="text"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  font-family: inherit;
}

.form-group input:disabled,
.form-group select:disabled,
.form-group textarea:disabled {
  background: #fafafa;
  cursor: not-allowed;
}

.form-group small {
  display: block;
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.file-upload {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.file-upload input[type="file"] {
  display: none;
}

.file-upload .selected-file {
  font-size: 13px;
  color: #666;
  flex: 1;
  min-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-list {
  margin-top: 12px;
  padding: 12px;
  background: #fafafa;
  border: 1px solid #eee;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  background: #fff;
  border: 1px solid #eee;
  margin-bottom: 6px;
}

.file-item:last-child {
  margin-bottom: 0;
}

.file-info {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.file-name {
  font-size: 14px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  font-size: 12px;
  color: #999;
  flex-shrink: 0;
}

.btn-icon {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 16px;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.btn-icon:hover {
  color: #cc0000;
}

.btn-icon:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.file-hint {
  margin-top: 8px;
}

.file-hint small {
  color: #e65100;
}

.form-actions {
  display: flex;
  gap: 8px;
  margin-top: 24px;
  padding-top: 24px;
  border-top: 1px solid #eee;
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
