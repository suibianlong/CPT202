<template>
  <div class="resource-form-container">
    <!-- 左侧表单列 -->
    <div class="form-column" :class="{ 'full-width': !showPreview }">
      <div class="resource-form">
        <div class="page-header">
          <h2>{{ isEdit ? '编辑遗产' : '贡献新遗产' }}</h2>
          <div class="header-actions">
            <!-- 版本历史按钮 -->
            <button 
              v-if="isEdit && !isReadOnly" 
              class="btn-version" 
              @click="showVersionHistory = true"
              title="查看版本历史"
            >
              📜 版本历史
            </button>
            <div v-if="autoSaveStatus" class="auto-save-indicator" :class="{ 'has-error': saveError }">
              <span v-if="!saveError" class="pulse"></span>
              <span>{{ autoSaveStatus }}</span>
              <button v-if="saveError" class="retry-btn" @click="retryAutoSave">重试</button>
            </div>
          </div>
        </div>

        <!-- 重提历史 -->
        <div v-if="rejectionHistory.length > 0" class="resubmit-history">
          <details>
            <summary>📋 修订历史 ({{ rejectionHistory.length }}次重提)</summary>
            <div class="history-list">
              <div v-for="(history, index) in rejectionHistory" :key="index" class="history-item">
                <span class="history-number">第{{ history.resubmit_count }}次重提</span>
                <span class="history-time">{{ formatDateTime(history.resubmit_time) }}</span>
              </div>
            </div>
          </details>
        </div>

        <div v-if="draftRestored" class="alert alert-info">
          📋 已从自动保存恢复草稿，您的更改会自动保存。
        </div>

        <div v-if="submitSuccess" class="alert alert-success">
          ✅ 提交成功！资源正在等待审核。
        </div>

        <div v-if="errorMessage" class="alert alert-error">{{ errorMessage }}</div>

        <form @submit.prevent="handleSubmit">
          <!-- 驳回资源反馈横幅 -->
          <div v-if="rejectionFeedback" class="feedback-banner">
            <div class="feedback-header">
              <span class="feedback-icon">!</span>
              <strong>资源被驳回，请根据以下意见修改。</strong>
              <button class="btn-text" @click="toggleFeedback">{{ showFeedback ? '隐藏' : '显示' }}</button>
            </div>
            <div v-if="showFeedback" class="feedback-content">
              <div class="feedback-item">
                <strong>审核时间:</strong> {{ formatDateTime(rejectionFeedback.reviewed_at) }}
              </div>
              <div class="feedback-item">
                <strong>审核人:</strong> {{ rejectionFeedback.reviewerName || '未知' }}
              </div>
              <div v-if="rejectionFeedback.feedback_comment" class="feedback-item">
                <strong>审核意见:</strong>
                <div class="feedback-comment">{{ rejectionFeedback.feedback_comment }}</div>
              </div>
              
              <!-- 可点击的意见列表 -->
              <div v-if="parsedFeedbackItems.length > 0" class="feedback-items-list">
                <div 
                  v-for="item in parsedFeedbackItems" 
                  :key="item.field"
                  class="feedback-item-link"
                  @click="scrollToField(item.field)"
                >
                  <span class="feedback-bullet">•</span>
                  <span>{{ item.message }}</span>
                  <span class="feedback-badge">点击定位</span>
                </div>
              </div>
              
              <div v-if="resourceResubmitCount > 0" class="resubmit-info">
                📝 第 {{ resourceResubmitCount }} 次修订重提
              </div>
            </div>
          </div>

          <!-- 标题字段 -->
          <div class="form-group" :class="{ 'highlighted': isFieldHighlighted('title') }">
            <label for="title">标题 <span class="required">*</span></label>
            <input
              id="title"
              v-model="formData.title"
              type="text"
              required
              :disabled="isReadOnly"
              placeholder="输入资源标题"
              maxlength="255"
              @input="trackFieldChange('title')"
            />
            <span v-if="isFieldHighlighted('title')" class="field-status">✓ 已处理</span>
          </div>

          <!-- 分类字段 -->
          <div class="form-group" :class="{ 'highlighted': isFieldHighlighted('category_id') }">
            <label for="category">分类 <span class="required">*</span></label>
            <select 
              id="category" 
              v-model="formData.category_id" 
              required 
              :disabled="isReadOnly"
              @change="trackFieldChange('category_id')"
            >
              <option value="">-- 请选择分类 --</option>
              <option 
                v-for="cat in categories" 
                :key="cat.id" 
                :value="cat.id"
              >
                {{ cat.category_topic || cat.category_name || cat.name }}
              </option>
            </select>
            <span v-if="isFieldHighlighted('category_id')" class="field-status">✓ 已处理</span>
            <div v-if="categories.length === 0" class="loading-categories">
              加载分类中...
            </div>
          </div>

          <!-- 地点字段 -->
          <div class="form-group" :class="{ 'highlighted': isFieldHighlighted('place') }">
            <label for="place">地点</label>
            <input
              id="place"
              v-model="formData.place"
              type="text"
              :disabled="isReadOnly"
              placeholder="输入相关地点"
              maxlength="255"
              @input="trackFieldChange('place')"
            />
            <span v-if="isFieldHighlighted('place')" class="field-status">✓ 已处理</span>
          </div>

          <!-- 描述字段 -->
          <div class="form-group" :class="{ 'highlighted': isFieldHighlighted('description') }">
            <label for="description">描述 <span class="required">*</span></label>
            <textarea
              id="description"
              v-model="formData.description"
              rows="5"
              required
              :disabled="isReadOnly"
              placeholder="详细描述此资源（支持Markdown格式）"
              @input="trackFieldChange('description')"
            ></textarea>
            <span v-if="isFieldHighlighted('description')" class="field-status">✓ 已处理</span>
            <small>支持 Markdown 格式：**粗体**、*斜体*、[链接](url)、`代码`等</small>
          </div>

          <!-- 标签字段 -->
          <div class="form-group" :class="{ 'highlighted': isFieldHighlighted('tag_names') }">
            <label for="tags">标签</label>
            <input
              id="tags"
              v-model="tagsInput"
              type="text"
              :disabled="isReadOnly"
              placeholder="多个标签请用逗号分隔"
              @input="trackFieldChange('tag_names')"
            />
            <small>输入标签名称，用逗号分隔</small>
            <span v-if="isFieldHighlighted('tag_names')" class="field-status">✓ 已处理</span>
          </div>

          <!-- 文件上传区域 -->
          <div class="form-group">
            <label>文件 <span class="required">*</span> (至少需要1个文件)</label>
            
            <!-- 现有文件列表 -->
            <div class="file-list" v-if="uploadedFiles.length > 0">
              <div class="file-list-header">
                <span>已上传文件 ({{ uploadedFiles.length }})</span>
              </div>
              <div v-for="file in uploadedFiles" :key="file.file_id" class="file-item">
                <div class="file-info">
                  <span class="file-icon">📄</span>
                  <span class="file-name" :title="file.original_filename">
                    {{ file.original_filename || '未命名文件' }}
                  </span>
                  <span class="file-size" v-if="file.file_size">
                    ({{ formatFileSize(file.file_size) }})
                  </span>
                </div>
                <button
                  v-if="!isReadOnly"
                  type="button"
                  class="btn-icon"
                  @click="deleteFile(file.file_id)"
                  :disabled="deletingFile === file.file_id"
                  title="删除文件"
                >
                  ✕
                </button>
              </div>
            </div>
            
            <!-- 无文件提示 -->
            <div v-else-if="!loading && isEdit" class="no-files-message">
              <span>📂 暂无上传文件</span>
              <small>请至少上传1个文件后再提交审核。</small>
            </div>
            
            <!-- 加载中提示 -->
            <div v-if="loading && isEdit" class="loading-files">
              加载文件中...
            </div>

            <!-- 文件上传区域 -->
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
                📁 选择文件
              </button>
              <span v-if="selectedFile" class="selected-file">{{ selectedFile.name }}</span>
              <button
                v-if="selectedFile"
                type="button"
                class="btn-primary"
                @click="uploadFile"
                :disabled="uploading"
              >
                {{ uploading ? '上传中...' : '上传' }}
              </button>
            </div>

            <div v-if="uploadedFiles.length === 0 && !isReadOnly && !loading" class="file-hint">
              <small>⚠️ 未上传文件。提交审核至少需要1个文件。</small>
            </div>
          </div>

          <!-- 版权声明字段 -->
          <div class="form-group" :class="{ 'highlighted': isFieldHighlighted('copyright_declaration') }">
            <label for="copyright_declaration">版权声明 <span class="required">*</span></label>
            <textarea
              id="copyright_declaration"
              v-model="formData.copyright_declaration"
              rows="2"
              required
              :disabled="isReadOnly"
              placeholder="声明版权和许可信息"
              @input="trackFieldChange('copyright_declaration')"
            ></textarea>
            <span v-if="isFieldHighlighted('copyright_declaration')" class="field-status">✓ 已处理</span>
          </div>

          <!-- 使用声明字段 -->
          <div class="form-group">
            <label for="usage_declaration">使用声明</label>
            <textarea
              id="usage_declaration"
              v-model="formData.usage_declaration"
              rows="2"
              :disabled="isReadOnly"
              placeholder="声明此资源的使用方式"
            ></textarea>
          </div>

          <!-- 表单操作按钮 -->
          <div class="form-actions">
            <button
              type="button"
              class="btn-secondary"
              @click="saveDraft"
              :disabled="loading"
              v-if="!isReadOnly"
            >
              💾 保存草稿
            </button>
            <button
              type="submit"
              class="btn-primary"
              :disabled="loading || uploadedFiles.length === 0"
              v-if="!isReadOnly"
            >
              {{ loading ? '提交中...' : (isResubmit ? '🔄 重新提交审核' : '📮 提交审核') }}
            </button>
            <button type="button" class="btn-secondary" @click="goBack">
              返回
            </button>
          </div>
        </form>
      </div>
    </div>

    <!-- 右侧预览列 -->
    <div class="preview-column" v-if="showPreview">
      <PreviewPanel
        :formData="formData"
        :files="uploadedFiles"
        :categories="categories"
        @fullscreen-change="handleFullscreenChange"
      />
    </div>

    <!-- 切换预览按钮 -->
    <button class="toggle-preview-btn" @click="showPreview = !showPreview">
      {{ showPreview ? '▶ 隐藏预览' : '◀ 显示预览' }}
    </button>

    <!-- 版本历史组件 -->
    <VersionHistory
      v-if="showVersionHistory && resourceId"
      :resourceId="resourceId"
      :currentFormData="formData"
      @close="showVersionHistory = false"
      @rollback="handleRollback"
      @error="handleVersionError"
    />
  </div>
</template>

<script>
import axios from 'axios'
import { initAutoSave } from '../../utils/autoSave'
import { getLatestDraft } from '../../utils/localStorage'
import PreviewPanel from '../../components/PreviewPanel.vue'
import VersionHistory from '../../components/VersionHistory.vue'
import VersionService from '../../services/versionService'

const API_BASE = '/api/resources'

export default {
  name: 'ResourceForm',
  components: {
    PreviewPanel,
    VersionHistory
  },
  data() {
    return {
      // 预览相关
      showPreview: true,
      
      // 资源信息
      resourceId: null,
      isEdit: false,
      isReadOnly: false,
      
      // 加载状态
      loading: false,
      submitSuccess: false,
      errorMessage: '',
      requireFiles: false,
      
      // 表单数据
      originalData: null,
      categories: [],
      tagsInput: '',
      formData: {
        title: '',
        category_id: '',
        place: '',
        description: '',
        copyright_declaration: '',
        usage_declaration: '',
        tag_names: []
      },
      
      // 文件相关
      selectedFile: null,
      uploadedFiles: [],
      uploading: false,
      deletingFile: null,
      
      // 自动保存相关
      autoSaveService: null,
      autoSaveStatus: '',
      lastAutoSave: null,
      saveError: null,
      hasUnsavedLocalChanges: false,
      draftRestored: false,
      
      // 驳回反馈相关
      rejectionFeedback: null,
      showFeedback: true,
      feedbackFields: [],
      addressedFields: new Set(),
      resourceResubmitCount: 0,
      rejectionHistory: [],
      parsedFeedbackItems: [],
      
      // 版本历史相关
      showVersionHistory: false,
      versionService: null
    }
  },
  computed: {
    isResubmit() {
      return this.rejectionFeedback !== null
    }
  },
  watch: {
    formData: {
      handler(newVal) {
        this.hasUnsavedLocalChanges = true
        if (this.autoSaveService) {
          this.autoSaveService.onFormChange(newVal)
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
  mounted() {
    const resourceId = this.$route.params.id
    
    this.fetchCategories().then(() => {
      if (resourceId && resourceId !== 'new') {
        this.initForm(resourceId)
        this.initVersionService()
      }
    })
    
    setTimeout(() => {
      this.initializeAutoSave()
    }, 1000)
    
    window.addEventListener('beforeunload', this.beforeUnloadHandler)
  },
  beforeUnmount() {
    if (this.autoSaveService) {
      this.autoSaveService.destroy()
    }
    window.removeEventListener('beforeunload', this.beforeUnloadHandler)
  },
  beforeRouteLeave(to, from, next) {
    if (this.hasUnsavedLocalChanges) {
      const answer = window.confirm('您有未保存的更改，确定要离开吗？')
      if (!answer) {
        next(false)
        return
      }
    }
    next()
  },
  methods: {
    // ==================== 工具方法 ====================
    formatTime(date) {
      if (!date) return ''
      const d = new Date(date)
      return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}:${String(d.getSeconds()).padStart(2, '0')}`
    },
    
    formatDateTime(dateStr) {
      if (!dateStr) return '-'
      const date = new Date(dateStr)
      return date.toLocaleString('zh-CN', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit',
        hour: '2-digit',
        minute: '2-digit'
      })
    },
    
    formatFileSize(bytes) {
      if (!bytes) return '0 B'
      const k = 1024
      const sizes = ['B', 'KB', 'MB', 'GB']
      const i = Math.floor(Math.log(bytes) / Math.log(k))
      return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
    },
    
    showMessage(message, type = 'info') {
      if (this.$message) {
        this.$message[type](message)
      } else {
        console.log(`${type}:`, message)
        if (type === 'error') alert(message)
      }
    },
    
    showSuccessMessage(message) {
      this.showMessage(message, 'success')
    },
    
    // ==================== 预览相关 ====================
    handleFullscreenChange(isFullscreen) {
      if (isFullscreen) {
        document.querySelector('.form-column')?.style.setProperty('width', '50%', 'important')
      } else {
        document.querySelector('.form-column')?.style.removeProperty('width')
      }
    },
    
    // ==================== 字段联动滚动 ====================
    parseFeedbackToItems(feedbackComment) {
      if (!feedbackComment) return []
      
      const fieldMap = {
        'title': { field: 'title', message: '标题需要修改', selector: '#title' },
        'description': { field: 'description', message: '描述需要完善', selector: '#description' },
        'category': { field: 'category_id', message: '分类选择有误', selector: '#category' },
        'place': { field: 'place', message: '地点信息需要补充', selector: '#place' },
        'copyright': { field: 'copyright_declaration', message: '版权声明不规范', selector: '#copyright_declaration' },
        'tag': { field: 'tag_names', message: '标签需要调整', selector: '#tags' },
        'file': { field: 'file', message: '文件需要补充/替换', selector: '.file-upload' }
      }
      
      const items = []
      const lowerComment = feedbackComment.toLowerCase()
      
      for (const [keyword, info] of Object.entries(fieldMap)) {
        if (lowerComment.includes(keyword)) {
          items.push(info)
        }
      }
      
      if (items.length === 0 && feedbackComment) {
        items.push({
          field: 'general',
          message: feedbackComment.substring(0, 100),
          selector: null
        })
      }
      
      return items
    },
    
    scrollToField(fieldName) {
      let selector = ''
      switch(fieldName) {
        case 'title':
          selector = '#title'
          break
        case 'description':
          selector = '#description'
          break
        case 'category_id':
          selector = '#category'
          break
        case 'place':
          selector = '#place'
          break
        case 'copyright_declaration':
          selector = '#copyright_declaration'
          break
        case 'tag_names':
          selector = '#tags'
          break
        case 'file':
          selector = '.file-upload'
          break
        default:
          selector = null
      }
      
      if (selector) {
        const element = document.querySelector(selector)
        if (element) {
          element.scrollIntoView({ 
            behavior: 'smooth', 
            block: 'center'
          })
          
          element.style.transition = 'all 0.3s'
          element.style.boxShadow = '0 0 0 2px #ff9800'
          element.style.backgroundColor = '#fff8e1'
          
          setTimeout(() => {
            element.style.boxShadow = ''
            element.style.backgroundColor = ''
          }, 2000)
          
          if (element.tagName === 'INPUT' || element.tagName === 'TEXTAREA' || element.tagName === 'SELECT') {
            element.focus()
          }
        }
      }
    },
    
    // ==================== 版本历史相关 ====================
    initVersionService() {
      if (this.resourceId) {
        this.versionService = new VersionService(this.resourceId)
      }
    },
    
    async saveVersionSnapshot(changeType) {
      if (this.versionService && this.resourceId) {
        await this.versionService.saveVersion(this.formData, changeType)
      }
    },
    
    handleRollback(formData) {
      this.formData = { ...formData }
      this.tagsInput = this.formData.tag_names?.join(', ') || ''
      this.showVersionHistory = false
      this.showSuccessMessage('已成功回滚到选定版本')
      this.saveVersionSnapshot('rollback')
    },
    
    handleVersionError(error) {
      this.showMessage(error, 'error')
    },
    
    // ==================== 自动保存相关 ====================
    initializeAutoSave() {
      const resourceId = this.$route.params.id || 'new'
      const savedDraft = getLatestDraft(resourceId)

      if (savedDraft && this.isEdit) {
        this.draftRestored = true
        this.formData = savedDraft
        this.autoSaveStatus = '已从自动保存恢复草稿'
        this.originalData = { ...savedDraft }
      }

      this.autoSaveService = initAutoSave({
        resourceId: resourceId,
        onAutoSave: (data) => {
          this.autoSaveStatus = '检测到更改，即将自动保存...'
        },
        onSaveSuccess: (result) => {
          this.lastAutoSave = new Date(result.savedAt)
          this.autoSaveStatus = `已自动保存于 ${this.formatTime(this.lastAutoSave)}`
          this.saveError = null
          this.hasUnsavedLocalChanges = false
        },
        onSaveError: (error) => {
          this.saveError = error.error
          this.autoSaveStatus = `保存失败: ${error.error}`
          if (error.savedLocally) {
            this.autoSaveStatus += ' (已保存到本地)'
          }
        },
        onRetry: () => {
          if (this.autoSaveService) {
            this.autoSaveService.manualSave()
          }
        }
      })
    },
    
    retryAutoSave() {
      if (this.autoSaveService) {
        this.autoSaveService.manualSave()
      }
    },
    
    beforeUnloadHandler(e) {
      if (this.hasUnsavedLocalChanges) {
        e.preventDefault()
        e.returnValue = '您有未保存的更改，确定要离开吗？'
        return e.returnValue
      }
    },
    
    // ==================== 驳回反馈相关 ====================
    toggleFeedback() {
      this.showFeedback = !this.showFeedback
    },
    
    isFieldHighlighted(fieldName) {
      return this.feedbackFields.includes(fieldName) && this.addressedFields.has(fieldName)
    },
    
    trackFieldChange(fieldName) {
      if (this.feedbackFields.includes(fieldName)) {
        this.addressedFields.add(fieldName)
      }
    },
    
    parseFeedbackFields(feedbackComment) {
      if (!feedbackComment) return []

      const fieldKeywords = {
        'title': ['title', '标题'],
        'description': ['description', '描述', '说明'],
        'category_id': ['category', '分类', '类别'],
        'place': ['location', 'place', '地点', '位置'],
        'copyright_declaration': ['copyright', '版权', '版权声明'],
        'tag_names': ['tag', '标签']
      }

      const matchedFields = []
      const comment = feedbackComment.toLowerCase()

      for (const [field, keywords] of Object.entries(fieldKeywords)) {
        if (keywords.some(k => comment.includes(k))) {
          matchedFields.push(field)
        }
      }

      return matchedFields
    },
    
    async loadRejectionFeedback() {
      if (!this.resourceId) return

      try {
        const response = await axios.get(`${API_BASE}/${this.resourceId}/rejection-feedback`)
        if (response.data) {
          this.rejectionFeedback = response.data
          this.feedbackFields = this.parseFeedbackFields(response.data.feedback_comment)
          this.parsedFeedbackItems = this.parseFeedbackToItems(response.data.feedback_comment)
        }
      } catch (error) {
        console.error('Failed to load rejection feedback:', error)
      }
    },
    
    async loadRejectionHistory() {
      try {
        const response = await axios.get(`${API_BASE}/${this.resourceId}`)
        this.rejectionHistory = response.data.rejection_history || []
        this.resourceResubmitCount = response.data.resubmit_count || 0
      } catch (error) {
        console.error('Load rejection history failed:', error)
      }
    },
    
    // ==================== API 调用 ====================
    async fetchCategories() {
      try {
        console.log('Fetching categories...')
        const response = await axios.get(`${API_BASE}/categories`)
        console.log('Categories API response:', response.data)
        
        let categoriesData = response.data || []
        if (Array.isArray(categoriesData)) {
          this.categories = categoriesData
        } else {
          this.categories = []
        }
        
        if (this.categories.length === 0) {
          this.categories = [
            { id: 1, category_topic: '文化', category_name: 'Culture' },
            { id: 2, category_topic: '历史', category_name: 'History' },
            { id: 3, category_topic: '艺术', category_name: 'Art' },
            { id: 4, category_topic: '科学', category_name: 'Science' },
            { id: 5, category_topic: '技术', category_name: 'Technology' }
          ]
        }
      } catch (error) {
        console.error('Fetch categories failed:', error)
        this.categories = [
          { id: 1, category_topic: '文化', category_name: 'Culture' },
          { id: 2, category_topic: '历史', category_name: 'History' },
          { id: 3, category_topic: '艺术', category_name: 'Art' },
          { id: 4, category_topic: '科学', category_name: 'Science' },
          { id: 5, category_topic: '技术', category_name: 'Technology' }
        ]
      }
    },
    
    async fetchFiles() {
      if (!this.resourceId) return
      
      try {
        console.log(`Fetching files for resource ${this.resourceId}...`)
        const response = await axios.get(`${API_BASE}/${this.resourceId}/files`)
        console.log('Files API response:', response.data)
        
        let files = []
        if (Array.isArray(response.data)) {
          files = response.data
        } else if (response.data.files && Array.isArray(response.data.files)) {
          files = response.data.files
        } else if (response.data.data && Array.isArray(response.data.data)) {
          files = response.data.data
        }
        
        this.uploadedFiles = files.map(file => ({
          file_id: file.file_id || file.id,
          original_filename: file.original_filename || file.file_name || file.name || '未知文件',
          file_size: file.file_size || file.size || 0,
          file_path: file.file_path,
          mime_type: file.mime_type || file.type,
          uploaded_at: file.uploaded_at
        }))
        
        console.log(`Loaded ${this.uploadedFiles.length} files`)
      } catch (error) {
        console.error('Fetch files failed:', error)
        this.uploadedFiles = []
      }
    },
    
    async initForm(resourceId) {
      this.loading = true
      this.resourceId = resourceId

      try {
        console.log('Initializing form for resource:', resourceId)
        const response = await axios.get(`${API_BASE}/${resourceId}`)
        const resource = response.data

        console.log('Resource data:', resource)

        this.isEdit = true
        this.isReadOnly = resource.status === 'Pending Review'
        
        this.resourceResubmitCount = resource.resubmit_count || 0
        this.rejectionHistory = resource.rejection_history || []

        const tagNames = resource.tags?.map(t => t.tag_name) || []

        this.formData = {
          title: resource.title || '',
          category_id: resource.categories?.[0]?.id || '',
          place: resource.place || '',
          description: resource.description || '',
          copyright_declaration: resource.copyright_declaration || '',
          usage_declaration: resource.usage_declaration || '',
          tag_names: tagNames
        }

        this.tagsInput = tagNames.join(', ')
        this.originalData = { ...this.formData }

        await this.fetchFiles()

        if (resource.status === 'Rejected') {
          await this.loadRejectionFeedback()
        }

      } catch (error) {
        console.error('Init form failed:', error)
        this.errorMessage = '加载资源失败'
      } finally {
        this.loading = false
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
    
    // ==================== 草稿保存 ====================
    async saveDraft(manual = true) {
      this.loading = true
      this.errorMessage = ''
      this.requireFiles = false

      try {
        const payload = this.buildPayload()

        if (this.isEdit && this.resourceId) {
          await axios.post(`${API_BASE}/${this.resourceId}/draft`, payload)
          await this.saveVersionSnapshot('edit')
        } else {
          const response = await axios.post(API_BASE, payload)
          this.resourceId = response.data.resource_id || response.data.id
          this.isEdit = true
          this.initVersionService()
          await this.saveVersionSnapshot('create')
          if (this.autoSaveService) {
            this.autoSaveService.onFormChange(this.formData)
          }
        }

        this.originalData = { ...this.formData }
        this.hasUnsavedLocalChanges = false
        this.autoSaveStatus = manual ? '草稿保存成功' : ''
        this.lastAutoSave = new Date()
        this.$emit('refresh-list')
        
        if (manual) {
          this.showSuccessMessage('草稿保存成功')
        }
        
      } catch (error) {
        this.errorMessage = error.response?.data?.error || '保存草稿失败'
        this.autoSaveStatus = `保存失败: ${this.errorMessage}`
        if (this.autoSaveService) {
          this.autoSaveService.saveToLocalStorage(this.formData, this.resourceId)
        }
      } finally {
        this.loading = false
      }
    },
    
    // ==================== 提交审核 ====================
    async handleSubmit() {
      this.loading = true
      this.errorMessage = ''
      this.requireFiles = false
      this.submitSuccess = false

      try {
        if (!this.formData.title || !this.formData.description || !this.formData.copyright_declaration) {
          this.errorMessage = '请填写所有必填字段：标题、描述和版权声明'
          this.loading = false
          return
        }

        if (this.uploadedFiles.length === 0) {
          this.requireFiles = true
          this.errorMessage = '请至少上传1个文件后再提交审核'
          this.loading = false
          return
        }

        if (this.autoSaveService && this.autoSaveService.hasUnsavedChanges()) {
          await this.autoSaveService.manualSave(false)
        }

        if (!this.resourceId) {
          const payload = this.buildPayload()
          const response = await axios.post(API_BASE, payload)
          this.resourceId = response.data.resource_id || response.data.id
          this.isEdit = true
          this.initVersionService()
        }

        const response = await axios.post(`${API_BASE}/${this.resourceId}/submit`)
        
        await this.saveVersionSnapshot('submit')
        
        if (response.data.is_resubmit) {
          this.resourceResubmitCount = response.data.resubmit_count
          this.showMessage(`第 ${response.data.resubmit_count} 次修订已重新提交`, 'success')
          await this.loadRejectionHistory()
        }
        
        this.submitSuccess = true
        this.autoSaveStatus = '已提交审核'
        
        if (this.autoSaveService) {
          this.autoSaveService.clearChanges()
          this.autoSaveService.clearBackups(this.resourceId)
        }

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
          this.errorMessage = errorData?.error || '提交审核失败'
        }
        this.autoSaveStatus = `提交失败: ${this.errorMessage}`
      } finally {
        this.loading = false
      }
    },
    
    async reloadResource() {
      try {
        const response = await axios.get(`${API_BASE}/${this.resourceId}`)
        const resource = response.data

        this.formData = {
          title: resource.title || '',
          category_id: resource.categories?.[0]?.id || '',
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
    
    // ==================== 文件操作 ====================
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
    this.errorMessage = '请先保存草稿再上传文件'
    return
  }

  this.uploading = true
  this.errorMessage = ''

  try {
    const formData = new FormData()
    formData.append('file', this.selectedFile)  // 注意：字段名必须是 'file'

    console.log('Uploading file:', this.selectedFile.name)
    console.log('Resource ID:', this.resourceId)

    const response = await axios.post(
      `${API_BASE}/${this.resourceId}/upload`,
      formData,
      {
        headers: {
          'Content-Type': 'multipart/form-data'
        }
      }
    )

    console.log('Upload response:', response.data)

    this.uploadedFiles.unshift(response.data.file)
    this.selectedFile = null
    this.$refs.fileInput.value = ''
    this.saveVersionSnapshot('edit')

  } catch (error) {
    console.error('Upload error:', error)
    console.error('Error response:', error.response)
    this.errorMessage = error.response?.data?.error || '上传文件失败'
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
        this.saveVersionSnapshot('edit')
      } catch (error) {
        this.errorMessage = error.response?.data?.error || '删除文件失败'
      } finally {
        this.deletingFile = null
      }
    },
    
    // ==================== 导航 ====================
    goBack() {
      this.$router.push('/contributor/my-submissions')
    }
  }
}
</script>

<style scoped>
.resource-form-container {
  display: flex;
  gap: 0;
  min-height: calc(100vh - 60px);
}

.form-column {
  flex: 1;
  min-width: 0;
  transition: width 0.3s;
  overflow-y: auto;
  padding: 20px;
}

.form-column.full-width {
  max-width: 100%;
}

.preview-column {
  width: 450px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  height: calc(100vh - 60px);
}

.resource-form {
  max-width: 800px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.page-header h2 {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.btn-version {
  background: #f5f5f5;
  border: 1px solid #e0e0e0;
  padding: 6px 12px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
}

.btn-version:hover {
  background: #e8e8e8;
}

.auto-save-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: #666;
  background: #f5f5f5;
  padding: 6px 12px;
  border-radius: 6px;
}

.auto-save-indicator.has-error {
  background: #ffebee;
  color: #c62828;
}

.pulse {
  width: 8px;
  height: 8px;
  background: #4caf50;
  border-radius: 50%;
  animation: pulse 1.5s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.8); }
}

.retry-btn {
  background: #c62828;
  color: white;
  border: none;
  padding: 2px 8px;
  border-radius: 3px;
  font-size: 11px;
  cursor: pointer;
  margin-left: 8px;
}

.retry-btn:hover {
  background: #b71c1c;
}

.toggle-preview-btn {
  position: fixed;
  right: 20px;
  bottom: 20px;
  background: #1a1a1a;
  color: white;
  border: none;
  padding: 10px 16px;
  border-radius: 30px;
  cursor: pointer;
  font-size: 14px;
  z-index: 100;
  box-shadow: 0 2px 8px rgba(0,0,0,0.2);
}

.toggle-preview-btn:hover {
  background: #333;
}

/* 响应式 */
@media (max-width: 768px) {
  .preview-column {
    position: fixed;
    right: 0;
    top: 0;
    height: 100vh;
    z-index: 200;
    box-shadow: -2px 0 8px rgba(0,0,0,0.1);
  }
  
  .form-column {
    padding: 16px;
  }
}

/* 其他样式保持原有不变 */
.alert {
  padding: 12px 16px;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
}

.alert-success {
  background: #e8f5e9;
  color: #2e7d32;
  border: 1px solid #c8e6c9;
}

.alert-error {
  background: #ffebee;
  color: #c62828;
  border: 1px solid #ffcdd2;
}

.alert-info {
  background: #e3f2fd;
  color: #1565c0;
  border: 1px solid #bbdef5;
}

.resubmit-history {
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f5f5f5;
  border-radius: 8px;
}

.resubmit-history details {
  cursor: pointer;
}

.resubmit-history summary {
  font-size: 13px;
  font-weight: 500;
  color: #666;
}

.history-list {
  margin-top: 12px;
  padding-left: 20px;
}

.history-item {
  display: flex;
  gap: 16px;
  padding: 6px 0;
  font-size: 12px;
  border-bottom: 1px solid #e0e0e0;
}

.history-number {
  color: #1565c0;
  font-weight: 500;
}

.history-time {
  color: #999;
}

.feedback-banner {
  background: #fff8e1;
  border: 1px solid #ffe082;
  border-radius: 8px;
  padding: 16px;
  margin-bottom: 24px;
}

.feedback-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.feedback-icon {
  width: 24px;
  height: 24px;
  background: #f57c00;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.feedback-header strong {
  color: #e65100;
  flex: 1;
}

.feedback-header .btn-text {
  color: #999;
  background: none;
  border: none;
  cursor: pointer;
  font-size: 12px;
}

.feedback-content {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ffe082;
}

.feedback-item {
  margin-bottom: 8px;
}

.feedback-item strong {
  color: #666;
  font-size: 12px;
}

.feedback-item p {
  margin: 4px 0 0 0;
  color: #333;
  font-size: 14px;
}

.feedback-comment {
  margin-top: 8px;
  padding: 8px 12px;
  background: #fff3e0;
  border-radius: 6px;
  font-size: 14px;
  line-height: 1.6;
}

.feedback-items-list {
  margin-top: 12px;
  padding-top: 12px;
  border-top: 1px solid #ffe082;
}

.feedback-item-link {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  margin: 4px 0;
  background: #fff;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.feedback-item-link:hover {
  background: #fff3e0;
  transform: translateX(4px);
}

.feedback-bullet {
  color: #f57c00;
  font-weight: bold;
}

.feedback-badge {
  margin-left: auto;
  font-size: 10px;
  color: #999;
  background: #f0f0f0;
  padding: 2px 6px;
  border-radius: 10px;
}

.resubmit-info {
  margin-top: 8px;
  padding: 8px;
  background: #e3f2fd;
  border-radius: 4px;
  font-size: 13px;
  color: #1565c0;
}

.form-group {
  margin-bottom: 24px;
  position: relative;
}

.form-group.highlighted {
  padding: 16px;
  background: #e8f5e9;
  border: 1px solid #a5d6a7;
  border-radius: 8px;
  margin: -16px -16px 24px -16px;
}

.form-group.highlighted label {
  color: #2e7d32;
}

.field-status {
  position: absolute;
  top: 8px;
  right: 8px;
  background: #4caf50;
  color: white;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
  font-size: 14px;
  color: #333;
}

.required {
  color: #c62828;
}

.form-group input[type="text"],
.form-group select,
.form-group textarea {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #ddd;
  border-radius: 6px;
  font-size: 14px;
  font-family: inherit;
  transition: border-color 0.2s;
}

.form-group input:focus,
.form-group select:focus,
.form-group textarea:focus {
  outline: none;
  border-color: #1a1a1a;
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

.loading-categories {
  margin-top: 4px;
  font-size: 12px;
  color: #999;
}

.file-list {
  margin-top: 12px;
  margin-bottom: 12px;
  padding: 12px;
  background: #fafafa;
  border: 1px solid #eee;
  border-radius: 6px;
}

.file-list-header {
  padding: 8px 0;
  font-size: 13px;
  font-weight: 500;
  color: #666;
  border-bottom: 1px solid #eee;
  margin-bottom: 8px;
}

.file-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px;
  background: #fff;
  border: 1px solid #eee;
  border-radius: 6px;
  margin-bottom: 8px;
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

.file-icon {
  font-size: 16px;
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
}

.btn-icon {
  background: none;
  border: none;
  color: #999;
  cursor: pointer;
  font-size: 16px;
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: all 0.2s;
}

.btn-icon:hover {
  background: #ffebee;
  color: #c62828;
}

.no-files-message {
  padding: 20px;
  text-align: center;
  background: #fafafa;
  border: 1px dashed #ddd;
  border-radius: 6px;
  margin: 12px 0;
}

.no-files-message span {
  display: block;
  font-size: 14px;
  color: #999;
  margin-bottom: 8px;
}

.no-files-message small {
  font-size: 12px;
  color: #e65100;
}

.loading-files {
  padding: 12px;
  text-align: center;
  color: #999;
  font-size: 13px;
}

.file-upload {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-top: 12px;
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

.file-hint {
  margin-top: 8px;
}

.file-hint small {
  color: #e65100;
}

.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #eee;
}

.btn-primary {
  background: #1a1a1a;
  color: #fff;
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #333;
}

.btn-secondary {
  background: #f5f5f5;
  color: #333;
  padding: 10px 24px;
  border: 1px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover:not(:disabled) {
  background: #e8e8e8;
}

.btn-primary:disabled,
.btn-secondary:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>